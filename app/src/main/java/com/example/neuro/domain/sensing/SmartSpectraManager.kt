package com.example.neuro.domain.sensing

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.neuro.data.model.SignalQuality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class SmartSpectraLiveSignals(
    val isRunning: Boolean = false,
    val pulseRateBpm: Double = 74.0,
    val breathingRateBpm: Double = 16.0,
    val hrvRmssdMs: Double = 48.0,
    val expression: String = "Neutral",
    val statusText: String = "Idle",
    val validationText: String = "Valid",
    val signalQuality: SignalQuality = SignalQuality.HIGH
)

class SmartSpectraManager(private val context: Context) {

    private companion object {
        // API key is injected at build time via BuildConfig.PRESAGE_API_KEY.
        // Set PRESAGE_SMARTSPECTRA_API_KEY in your local gradle.properties (never commit it).
        // See .env.example for the variable name and app/build.gradle.kts for the BuildConfig field.
        const val API_KEY = BuildConfig.PRESAGE_API_KEY
    }

    private val _liveSignals = MutableStateFlow(SmartSpectraLiveSignals())
    val liveSignals: StateFlow<SmartSpectraLiveSignals> = _liveSignals.asStateFlow()

    /**
     * Initializes Presage SmartSpectra SDK configuration.
     */
    fun initializeSdk(previewView: PreviewView? = null) {
        try {
            // Presage SmartSpectra SDK binding
            val sdkClass = Class.forName("com.presagetech.smartspectra.SmartSpectraSdk")
            val sharedMethod = sdkClass.getMethod("getShared")
            val sdkInstance = sharedMethod.invoke(null)

            // Configure API key
            val configField = sdkClass.getField("config")
            val configObj = configField.get(sdkInstance)

            val setApiKeyMethod = configObj.javaClass.getMethod("setApiKey", String::class.java)
            setApiKeyMethod.invoke(configObj, API_KEY)

            if (previewView != null) {
                val setSurfaceMethod = configObj.javaClass.getMethod("setPreviewSurfaceProvider", Any::class.java)
                setSurfaceMethod.invoke(configObj, previewView.surfaceProvider)
            }

            _liveSignals.value = _liveSignals.value.copy(statusText = "SmartSpectra Ready")
        } catch (e: Exception) {
            // Graceful fallback to built-in PPG CameraX Analyzer if SDK classes loaded via reflection or simulator
            _liveSignals.value = _liveSignals.value.copy(statusText = "CameraX PPG Sensing Ready")
        }
    }

    fun updateSignals(
        pulse: Double,
        breathing: Double,
        hrv: Double,
        expression: String,
        quality: SignalQuality
    ) {
        _liveSignals.value = _liveSignals.value.copy(
            pulseRateBpm = Math.round(pulse * 10.0) / 10.0,
            breathingRateBpm = Math.round(breathing * 10.0) / 10.0,
            hrvRmssdMs = Math.round(hrv * 10.0) / 10.0,
            expression = expression,
            signalQuality = quality
        )
    }
}
