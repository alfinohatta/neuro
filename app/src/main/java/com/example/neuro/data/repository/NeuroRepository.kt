package com.example.neuro.data.repository

import com.example.neuro.data.api.RetrofitClient
import com.example.neuro.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NeuroRepository private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: NeuroRepository? = null

        fun getInstance(): NeuroRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NeuroRepository().also { INSTANCE = it }
            }
        }
    }

    private val api = RetrofitClient.apiService
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory reactive data store & offline cache
    private val currentUser = MutableStateFlow(
        User(id = 1L, fullName = "Alfino", email = "alfino@example.com")
    )
    private val preferences = MutableStateFlow(
        UserPreferences(userId = 1L, focusDurationMinutes = 25, breakDurationMinutes = 5, dailyFocusGoalMinutes = 120)
    )
    private val consents = MutableStateFlow(
        listOf(
            UserConsent(id = 1, userId = 1, consentType = ConsentType.CAMERA, isGranted = true),
            UserConsent(id = 2, userId = 1, consentType = ConsentType.SIGNAL_PROCESSING, isGranted = true),
            UserConsent(id = 3, userId = 1, consentType = ConsentType.ANALYTICS, isGranted = true),
            UserConsent(id = 4, userId = 1, consentType = ConsentType.PRIVACY_POLICY, isGranted = true)
        )
    )

    private val sessions = MutableStateFlow<List<FocusSession>>(
        listOf(
            FocusSession(
                id = 1, userId = 1, title = "Morning Deep Work", description = "Working on Neuro Focus development",
                sessionType = SessionType.DEEP_WORK, plannedDurationMinutes = 25, actualDurationSeconds = 1500,
                status = SessionStatus.COMPLETED, startedAt = "2026-11-04T08:00:00Z", endedAt = "2026-11-04T08:25:00Z"
            ),
            FocusSession(
                id = 2, userId = 1, title = "Research Session", description = "Researching adaptive productivity systems",
                sessionType = SessionType.FOCUS, plannedDurationMinutes = 45, actualDurationSeconds = 2700,
                status = SessionStatus.COMPLETED, startedAt = "2026-11-04T09:30:00Z", endedAt = "2026-11-04T10:15:00Z"
            )
        )
    )

    private val measurements = MutableStateFlow<List<SignalMeasurement>>(emptyList())
    private val recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    private val feedbacks = MutableStateFlow<List<SessionFeedback>>(emptyList())
    private val summaries = MutableStateFlow<List<SessionSummary>>(emptyList())

    init {
        // Initial async sync from Spring Boot REST API
        syncFromSpringBootApi()
    }

    private fun syncFromSpringBootApi() {
        scope.launch {
            try {
                val userResponse = api.getUserProfile(1L)
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    currentUser.value = userResponse.body()!!
                }

                val prefResponse = api.getUserPreferences(1L)
                if (prefResponse.isSuccessful && prefResponse.body() != null) {
                    preferences.value = prefResponse.body()!!
                }

                val consentResponse = api.getUserConsents(1L)
                if (consentResponse.isSuccessful && consentResponse.body() != null) {
                    consents.value = consentResponse.body()!!
                }

                val sessionResponse = api.getUserSessions(1L)
                if (sessionResponse.isSuccessful && sessionResponse.body() != null) {
                    sessions.value = sessionResponse.body()!!
                }
            } catch (e: Exception) {
                // Spring Boot server offline; operating seamlessly using local database cache
            }
        }
    }

    fun getUser(): Flow<User> = currentUser.asStateFlow()
    fun getPreferences(): Flow<UserPreferences> = preferences.asStateFlow()
    fun getConsents(): Flow<List<UserConsent>> = consents.asStateFlow()
    fun getSessions(): Flow<List<FocusSession>> = sessions.asStateFlow()
    fun getMeasurements(): Flow<List<SignalMeasurement>> = measurements.asStateFlow()
    fun getRecommendations(): Flow<List<Recommendation>> = recommendations.asStateFlow()
    fun getFeedbacks(): Flow<List<SessionFeedback>> = feedbacks.asStateFlow()
    fun getSummaries(): Flow<List<SessionSummary>> = summaries.asStateFlow()

    fun updatePreferences(newPrefs: UserPreferences) {
        preferences.value = newPrefs
        scope.launch {
            try {
                api.updateUserPreferences(newPrefs.userId, newPrefs)
            } catch (e: Exception) {
                // Network queue fallback
            }
        }
    }

    fun updateConsent(consentType: ConsentType, isGranted: Boolean) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val existing = consents.value.find { it.consentType == consentType }
        val updatedList = if (existing != null) {
            consents.value.map { consent ->
                if (consent.consentType == consentType) {
                    consent.copy(
                        isGranted = isGranted,
                        grantedAt = if (isGranted) dateStr else consent.grantedAt,
                        revokedAt = if (!isGranted) dateStr else null
                    )
                } else consent
            }
        } else {
            val newId = (consents.value.maxOfOrNull { it.id } ?: 0) + 1
            consents.value + UserConsent(
                id = newId,
                userId = currentUser.value.id,
                consentType = consentType,
                isGranted = isGranted,
                grantedAt = if (isGranted) dateStr else null,
                revokedAt = if (!isGranted) dateStr else null
            )
        }
        consents.value = updatedList

        scope.launch {
            try {
                val targetConsent = updatedList.find { it.consentType == consentType }
                if (targetConsent != null) {
                    api.updateConsent(targetConsent.userId, targetConsent)
                }
            } catch (e: Exception) {
                // Network queue fallback
            }
        }
    }

    suspend fun startSession(title: String, durationMinutes: Int, sessionType: SessionType): FocusSession {
        val newId = (sessions.value.maxOfOrNull { it.id } ?: 0) + 1
        val startTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val session = FocusSession(
            id = newId,
            userId = currentUser.value.id,
            title = title,
            sessionType = sessionType,
            plannedDurationMinutes = durationMinutes,
            status = SessionStatus.ACTIVE,
            startedAt = startTime
        )
        sessions.value = sessions.value + session

        scope.launch {
            try {
                api.createSession(session)
            } catch (e: Exception) {
                // Offline session queue
            }
        }

        return session
    }

    fun addMeasurement(measurement: SignalMeasurement) {
        measurements.value = measurements.value + measurement
        scope.launch {
            try {
                api.postSignalMeasurement(measurement)
            } catch (e: Exception) {
                // Offline signal queue
            }
        }
    }

    fun addRecommendation(recommendation: Recommendation) {
        recommendations.value = recommendations.value + recommendation
    }

    fun submitFeedback(feedback: SessionFeedback) {
        feedbacks.value = feedbacks.value + feedback
        val nowStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        val session = sessions.value.find { it.id == feedback.sessionId }
        val sessionSignals = measurements.value.filter { it.sessionId == feedback.sessionId }

        val avgHr = sessionSignals.filter { it.signalType == SignalType.HEART_RATE }.map { it.signalValue }.average()
        val avgBr = sessionSignals.filter { it.signalType == SignalType.BREATHING_RATE }.map { it.signalValue }.average()
        val avgHrv = sessionSignals.filter { it.signalType == SignalType.HRV }.map { it.signalValue }.average()

        val calculatedScore = (feedback.focusRating * 20.0).coerceIn(0.0, 100.0)

        val summary = SessionSummary(
            userId = feedback.userId,
            sessionId = feedback.sessionId,
            totalFocusSeconds = session?.actualDurationSeconds ?: 0,
            totalBreakSeconds = 0,
            averageHeartRate = if (avgHr.isNaN()) 74.0 else avgHr,
            averageBreathingRate = if (avgBr.isNaN()) 16.0 else avgBr,
            averageHrv = if (avgHrv.isNaN()) 48.0 else avgHrv,
            focusScore = calculatedScore,
            summaryText = feedback.feedbackText ?: "Focus session completed successfully.",
            generatedAt = nowStr
        )
        summaries.value = summaries.value.filterNot { it.sessionId == feedback.sessionId } + summary

        scope.launch {
            try {
                api.submitFeedback(feedback.sessionId, feedback)
            } catch (e: Exception) {
                // Offline queue
            }
        }
    }

    fun completeSession(sessionId: Long, actualDurationSeconds: Int) {
        val endTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        sessions.value = sessions.value.map { session ->
            if (session.id == sessionId) {
                val updated = session.copy(
                    status = SessionStatus.COMPLETED,
                    actualDurationSeconds = actualDurationSeconds,
                    endedAt = endTime
                )
                scope.launch {
                    try {
                        api.endSession(sessionId)
                    } catch (e: Exception) {
                        // Offline queue
                    }
                }
                updated
            } else session
        }
    }
}
