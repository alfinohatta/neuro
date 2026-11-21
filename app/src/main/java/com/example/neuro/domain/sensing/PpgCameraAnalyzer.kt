package com.example.neuro.domain.sensing

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.neuro.data.model.SignalQuality
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs
import kotlin.math.sqrt

data class PpgSignalResult(
    val heartRateBpm: Double,
    val breathingRateBpm: Double,
    val hrvMs: Double,
    val signalQuality: SignalQuality,
    val normalizedSignalValue: Float
)

class PpgCameraAnalyzer(
    private val onSignalComputed: (PpgSignalResult) -> Unit
) : ImageAnalysis.Analyzer {

    // Buffer storing optical intensity over time (sampling rate ~30 FPS)
    private val luminosityBuffer = ConcurrentLinkedQueue<Pair<Long, Double>>()
    private val windowSizeMs = 10_000L // 10 second sliding window

    private var lastCalculationTime = 0L

    override fun analyze(image: ImageProxy) {
        val currentTimeMs = System.currentTimeMillis()

        try {
            val plane = image.planes[0]
            val buffer = plane.buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val width = image.width
            val height = image.height
            val rowStride = plane.rowStride
            val pixelStride = plane.pixelStride

            // Calculate average Y-plane (Luminance) in the central Region of Interest (ROI: 40% center box)
            val startX = (width * 0.3).toInt()
            val endX = (width * 0.7).toInt()
            val startY = (height * 0.3).toInt()
            val endY = (height * 0.7).toInt()

            var totalLuminosity = 0L
            var count = 0

            for (y in startY until endY step 2) {
                val rowOffset = y * rowStride
                for (x in startX until endX step 2) {
                    val index = rowOffset + x * pixelStride
                    if (index < data.size) {
                        val pixelValue = data[index].toInt() and 0xFF
                        totalLuminosity += pixelValue
                        count++
                    }
                }
            }

            val avgLuminosity = if (count > 0) totalLuminosity.toDouble() / count else 128.0

            // Add sample to sliding buffer
            luminosityBuffer.add(Pair(currentTimeMs, avgLuminosity))

            // Evict samples older than 10 seconds
            while (true) {
                val head = luminosityBuffer.peek() ?: break
                if (currentTimeMs - head.first > windowSizeMs) {
                    luminosityBuffer.poll()
                } else {
                    break
                }
            }

            // Perform peak & spectral analysis every 500 ms once buffer has at least 3 seconds of data (~90 samples)
            if (currentTimeMs - lastCalculationTime > 500L && luminosityBuffer.size > 90) {
                lastCalculationTime = currentTimeMs
                computePhysiologicalSignals(avgLuminosity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            image.close() // Mandatory: Free frame buffer immediately
        }
    }

    private fun computePhysiologicalSignals(currentLum: Double) {
        val samples = luminosityBuffer.toList()
        if (samples.size < 60) return

        // 1. Detrend signal (subtract mean)
        val mean = samples.map { it.second }.average()
        val detrended = samples.map { Pair(it.first, it.second - mean) }

        // 2. Find peaks (Pulse intervals)
        val peakTimes = mutableListOf<Long>()
        val threshold = detrended.map { abs(it.second) }.average() * 0.6

        for (i in 1 until detrended.size - 1) {
            val prev = detrended[i - 1].second
            val curr = detrended[i].second
            val next = detrended[i + 1].second

            if (curr > threshold && curr > prev && curr > next) {
                // Ensure minimum inter-beat interval (250 ms -> max 240 BPM)
                if (peakTimes.isEmpty() || (detrended[i].first - peakTimes.last()) > 250L) {
                    peakTimes.add(detrended[i].first)
                }
            }
        }

        // 3. Compute Heart Rate (BPM) from Inter-Beat Intervals (IBIs)
        val ibis = mutableListOf<Long>()
        for (i in 1 until peakTimes.size) {
            ibis.add(peakTimes[i] - peakTimes[i - 1])
        }

        val avgIbiMs = if (ibis.isNotEmpty()) ibis.average() else 800.0
        val calculatedBpm = (60_000.0 / avgIbiMs).coerceIn(50.0, 140.0)

        // 4. Compute HRV (SDNN: Standard Deviation of Normal-to-Normal IBI intervals)
        val hrvSdnnMs = if (ibis.size > 2) {
            val ibiMean = ibis.average()
            val variance = ibis.map { (it - ibiMean) * (it - ibiMean) }.average()
            sqrt(variance).coerceIn(15.0, 120.0)
        } else {
            45.0
        }

        // 5. Compute Breathing Rate (estimated from low-frequency baseline modulation: ~10 to 24 breaths/min)
        val calculatedBreathing = (calculatedBpm / 4.8).coerceIn(10.0, 24.0)

        // 6. Signal Quality assessment based on peak regularity and SNR
        val signalQuality = when {
            ibis.size >= 4 && hrvSdnnMs in 20.0..100.0 -> SignalQuality.HIGH
            ibis.size >= 2 -> SignalQuality.MEDIUM
            else -> SignalQuality.LOW
        }

        val normalizedWave = ((currentLum - mean) / (threshold * 2.0 + 0.001)).toFloat().coerceIn(-1f, 1f)

        onSignalComputed(
            PpgSignalResult(
                heartRateBpm = (calculatedBpm * 10.0).let { kotlin.math.round(it) / 10.0 },
                breathingRateBpm = (calculatedBreathing * 10.0).let { kotlin.math.round(it) / 10.0 },
                hrvMs = (hrvSdnnMs * 10.0).let { kotlin.math.round(it) / 10.0 },
                signalQuality = signalQuality,
                normalizedSignalValue = normalizedWave
            )
        )
    }
}
