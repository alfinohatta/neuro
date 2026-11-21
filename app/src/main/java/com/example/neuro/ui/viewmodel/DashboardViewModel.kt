package com.example.neuro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuro.data.model.*
import com.example.neuro.data.repository.NeuroRepository
import kotlinx.coroutines.flow.*

data class DashboardUiState(
    val user: User = User(id = 1L, fullName = "Alfino", email = "alfino@example.com"),
    val preferences: UserPreferences = UserPreferences(userId = 1L),
    val consents: List<UserConsent> = emptyList(),
    val sessions: List<FocusSession> = emptyList(),
    val measurements: List<SignalMeasurement> = emptyList(),
    val recommendations: List<Recommendation> = emptyList(),
    val summaries: List<SessionSummary> = emptyList(),
    val focusScore: Int = 87,
    val deepWorkMinutes: Int = 255,
    val hrvValue: Int = 71,
    val breathingRate: Int = 15,
    val heartRate: Int = 72
)

class DashboardViewModel(
    private val repository: NeuroRepository = NeuroRepository.getInstance()
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(repository.getUser(), repository.getPreferences(), repository.getConsents()) { u, p, c -> Triple(u, p, c) },
        combine(repository.getSessions(), repository.getMeasurements()) { s, m -> Pair(s, m) },
        combine(repository.getRecommendations(), repository.getSummaries()) { r, sum -> Pair(r, sum) }
    ) { (user, prefs, consents), (sessions, measurements), (recs, summaries) ->

        val completedSessions = sessions.filter { it.status == SessionStatus.COMPLETED }
        val totalFocusMins = completedSessions.sumOf { it.plannedDurationMinutes }

        val latestHr = measurements.lastOrNull { it.signalType == SignalType.HEART_RATE }?.signalValue?.toInt() ?: 72
        val latestHrv = measurements.lastOrNull { it.signalType == SignalType.HRV }?.signalValue?.toInt() ?: 71
        val latestBr = measurements.lastOrNull { it.signalType == SignalType.BREATHING_RATE }?.signalValue?.toInt() ?: 15

        DashboardUiState(
            user = user,
            preferences = prefs,
            consents = consents,
            sessions = sessions,
            measurements = measurements,
            recommendations = recs,
            summaries = summaries,
            focusScore = if (summaries.isNotEmpty()) summaries.last().focusScore?.toInt() ?: 87 else 87,
            deepWorkMinutes = if (totalFocusMins > 0) totalFocusMins else 255,
            hrvValue = latestHrv,
            breathingRate = latestBr,
            heartRate = latestHr
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
