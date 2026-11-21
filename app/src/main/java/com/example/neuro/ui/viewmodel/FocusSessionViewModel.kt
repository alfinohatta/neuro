package com.example.neuro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuro.data.model.*
import com.example.neuro.data.repository.NeuroRepository
import com.example.neuro.domain.recommendation.AdaptiveRecommendationEngine
import com.example.neuro.domain.sensing.PpgSignalResult
import com.example.neuro.domain.sensing.PresageSpectraManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ActiveSessionUiState(
    val currentSession: FocusSession? = null,
    val elapsedTimeSeconds: Int = 0,
    val isPaused: Boolean = false,
    val currentHeartRate: Double = 72.0,
    val currentBreathingRate: Double = 16.0,
    val currentHrv: Double = 48.0,
    val expression: String = "Neutral",
    val signalQuality: SignalQuality = SignalQuality.HIGH,
    val activeRecommendation: Recommendation? = null
)

class FocusSessionViewModel(
    private val repository: NeuroRepository = NeuroRepository.getInstance()
) : ViewModel() {

    private val sensingManager = PresageSpectraManager()
    private val recommendationEngine = AdaptiveRecommendationEngine()

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sensingJob: Job? = null

    fun startSession(title: String, durationMinutes: Int, sessionTypeStr: String) {
        val type = when (sessionTypeStr) {
            "deep_work" -> SessionType.DEEP_WORK
            "break" -> SessionType.BREAK
            else -> SessionType.FOCUS
        }

        viewModelScope.launch {
            val session = repository.startSession(title, durationMinutes, type)
            _uiState.value = ActiveSessionUiState(currentSession = session)

            runTimer()
            observeSignals(session.id)
        }
    }

    fun updateRealPpgSignal(result: PpgSignalResult) {
        _uiState.value = _uiState.value.copy(
            currentHeartRate = result.heartRateBpm,
            currentBreathingRate = result.breathingRateBpm,
            currentHrv = result.hrvMs,
            signalQuality = result.signalQuality
        )

        val session = _uiState.value.currentSession ?: return
        val nowStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        // Save signal measurement to repository
        repository.addMeasurement(
            SignalMeasurement(
                userId = 1L,
                sessionId = session.id,
                provider = "Presage SmartSpectra SDK",
                signalType = SignalType.HEART_RATE,
                signalValue = result.heartRateBpm,
                unit = "bpm",
                signalQuality = result.signalQuality,
                measuredAt = nowStr
            )
        )
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isPaused) {
                    val newElapsed = _uiState.value.elapsedTimeSeconds + 1
                    _uiState.value = _uiState.value.copy(elapsedTimeSeconds = newElapsed)

                    // Check for adaptive recommendations
                    val state = _uiState.value
                    val recommendation = recommendationEngine.generateRecommendation(
                        sessionId = state.currentSession?.id ?: 1L,
                        userId = 1L,
                        elapsedSeconds = newElapsed,
                        heartRate = state.currentHeartRate,
                        breathingRate = state.currentBreathingRate,
                        hrv = state.currentHrv
                    )
                    if (recommendation != null && state.activeRecommendation == null) {
                        _uiState.value = _uiState.value.copy(activeRecommendation = recommendation)
                        repository.addRecommendation(recommendation)
                    }
                }
            }
        }
    }

    private fun observeSignals(sessionId: Long) {
        sensingJob?.cancel()
        sensingJob = viewModelScope.launch {
            sensingManager.observeLiveSignals(sessionId, userId = 1L).collect { measurement ->
                repository.addMeasurement(measurement)
                _uiState.value = when (measurement.signalType) {
                    SignalType.HEART_RATE -> _uiState.value.copy(currentHeartRate = measurement.signalValue, signalQuality = measurement.signalQuality)
                    SignalType.BREATHING_RATE -> _uiState.value.copy(currentBreathingRate = measurement.signalValue)
                    SignalType.HRV -> _uiState.value.copy(currentHrv = measurement.signalValue)
                    else -> _uiState.value
                }
            }
        }
    }

    fun togglePause() {
        _uiState.value = _uiState.value.copy(isPaused = !_uiState.value.isPaused)
    }

    fun dismissRecommendation() {
        _uiState.value = _uiState.value.copy(activeRecommendation = null)
    }

    fun submitFeedback(feedback: SessionFeedback) {
        viewModelScope.launch {
            repository.submitFeedback(feedback)
        }
    }

    fun finishSession() {
        timerJob?.cancel()
        sensingJob?.cancel()
        val session = _uiState.value.currentSession
        if (session != null) {
            repository.completeSession(session.id, _uiState.value.elapsedTimeSeconds)
        }
    }
}
