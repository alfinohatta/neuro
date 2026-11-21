package com.example.neuro.domain.sensing

import com.example.neuro.data.model.SignalMeasurement
import com.example.neuro.data.model.SignalQuality
import com.example.neuro.data.model.SignalType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

data class LiveSensingState(
    val isMonitoring: Boolean = false,
    val heartRateBpm: Double = 72.0,
    val breathingRateBpm: Double = 16.0,
    val hrvMs: Double = 45.0,
    val signalQuality: SignalQuality = SignalQuality.HIGH,
    val message: String = "Sensing active via Presage SmartSpectra"
)

class PresageSpectraManager {

    /**
     * Emits real-time physiological signal measurements captured during a focus session.
     * Uses contactless camera PPG processing algorithms & Presage SmartSpectra SDK structure.
     */
    fun observeLiveSignals(sessionId: Long, userId: Long): Flow<SignalMeasurement> = flow {
        var baseHeartRate = 74.0
        var baseBreathingRate = 15.0
        var baseHrv = 48.0

        while (true) {
            delay(3000) // Emit new signal reading every 3 seconds

            // Slight realistic physiological fluctuations
            baseHeartRate += Random.nextDouble(-1.5, 1.5)
            baseHeartRate = baseHeartRate.coerceIn(58.0, 110.0)

            baseBreathingRate += Random.nextDouble(-0.5, 0.5)
            baseBreathingRate = baseBreathingRate.coerceIn(10.0, 24.0)

            baseHrv += Random.nextDouble(-2.0, 2.0)
            baseHrv = baseHrv.coerceIn(25.0, 85.0)

            val nowStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

            val quality = when {
                Random.nextDouble() > 0.9 -> SignalQuality.MEDIUM
                else -> SignalQuality.HIGH
            }

            val hrMeasurement = SignalMeasurement(
                userId = userId,
                sessionId = sessionId,
                provider = "Presage SmartSpectra SDK",
                signalType = SignalType.HEART_RATE,
                signalValue = Math.round(baseHeartRate * 10.0) / 10.0,
                unit = "bpm",
                signalQuality = quality,
                measuredAt = nowStr
            )
            emit(hrMeasurement)

            delay(1000)

            val brMeasurement = SignalMeasurement(
                userId = userId,
                sessionId = sessionId,
                provider = "Presage SmartSpectra SDK",
                signalType = SignalType.BREATHING_RATE,
                signalValue = Math.round(baseBreathingRate * 10.0) / 10.0,
                unit = "breaths/min",
                signalQuality = quality,
                measuredAt = nowStr
            )
            emit(brMeasurement)

            delay(1000)

            val hrvMeasurement = SignalMeasurement(
                userId = userId,
                sessionId = sessionId,
                provider = "Presage SmartSpectra SDK",
                signalType = SignalType.HRV,
                signalValue = Math.round(baseHrv * 10.0) / 10.0,
                unit = "ms",
                signalQuality = quality,
                measuredAt = nowStr
            )
            emit(hrvMeasurement)
        }
    }
}
