package com.example.neuro.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "full_name", nullable = false)
    val fullName: String = "",
    @Column(nullable = false, unique = true)
    val email: String = "",
    @Column(name = "password_hash", nullable = false)
    val passwordHash: String = "",
    @Column(name = "avatar_url")
    val avatarUrl: String? = null,
    val timezone: String = "UTC",
    @Column(name = "account_status", nullable = false)
    val accountStatus: String = "active",
    @Column(name = "email_verified", nullable = false)
    val emailVerified: Boolean = false,
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "user_preferences")
data class UserPreferencesEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false, unique = true)
    val userId: Long = 0,
    @Column(name = "focus_duration_minutes", nullable = false)
    val focusDurationMinutes: Int = 25,
    @Column(name = "break_duration_minutes", nullable = false)
    val breakDurationMinutes: Int = 5,
    @Column(name = "daily_focus_goal_minutes", nullable = false)
    val dailyFocusGoalMinutes: Int = 120,
    @Column(name = "notifications_enabled", nullable = false)
    val notificationsEnabled: Boolean = true,
    @Column(name = "sound_enabled", nullable = false)
    val soundEnabled: Boolean = true,
    @Column(name = "adaptive_recommendations_enabled", nullable = false)
    val adaptiveRecommendationsEnabled: Boolean = true,
    val theme: String = "system"
)

@Entity
@Table(name = "user_consents")
data class UserConsentEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,
    @Column(name = "consent_type", nullable = false)
    val consentType: String = "camera",
    @Column(name = "consent_version", nullable = false)
    val consentVersion: String = "1.0",
    @Column(name = "privacy_policy_version")
    val privacyPolicyVersion: String? = "1.0",
    @Column(name = "is_granted", nullable = false)
    val isGranted: Boolean = false,
    @Column(name = "granted_at")
    val grantedAt: LocalDateTime? = null,
    @Column(name = "revoked_at")
    val revokedAt: LocalDateTime? = null
)

@Entity
@Table(name = "focus_sessions")
data class FocusSessionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,
    val title: String = "",
    val description: String? = null,
    @Column(name = "session_type", nullable = false)
    val sessionType: String = "focus",
    @Column(name = "planned_duration_minutes", nullable = false)
    val plannedDurationMinutes: Int = 25,
    @Column(name = "actual_duration_seconds", nullable = false)
    val actualDurationSeconds: Int = 0,
    val status: String = "pending",
    @Column(name = "started_at")
    val startedAt: LocalDateTime? = null,
    @Column(name = "ended_at")
    val endedAt: LocalDateTime? = null,
    @Column(name = "interruption_count", nullable = false)
    val interruptionCount: Int = 0,
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "signal_measurements")
data class SignalMeasurementEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,
    @Column(name = "session_id", nullable = false)
    val sessionId: Long = 0,
    val provider: String = "Presage SmartSpectra",
    @Column(name = "signal_type", nullable = false)
    val signalType: String = "heart_rate",
    @Column(name = "signal_value")
    val signalValue: BigDecimal? = null,
    val unit: String? = "bpm",
    @Column(name = "signal_quality", nullable = false)
    val signalQuality: String = "high",
    @Column(name = "measured_at", nullable = false)
    val measuredAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "recommendations")
data class RecommendationEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,
    @Column(name = "session_id", nullable = false)
    val sessionId: Long = 0,
    @Column(name = "recommendation_type", nullable = false)
    val recommendationType: String = "break",
    val title: String = "",
    val message: String = "",
    val reason: String? = null,
    val priority: String = "medium",
    val status: String = "pending",
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "session_feedback")
data class SessionFeedbackEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,
    @Column(name = "session_id", nullable = false, unique = true)
    val sessionId: Long = 0,
    @Column(name = "focus_rating", nullable = false)
    val focusRating: Int = 5,
    @Column(name = "mood_rating")
    val moodRating: Int? = null,
    @Column(name = "productivity_rating")
    val productivityRating: Int? = null,
    @Column(name = "feedback_text")
    val feedbackText: String? = null
)

@Entity
@Table(name = "session_summaries")
data class SessionSummaryEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,
    @Column(name = "session_id", nullable = false, unique = true)
    val sessionId: Long = 0,
    @Column(name = "total_focus_seconds", nullable = false)
    val totalFocusSeconds: Int = 0,
    @Column(name = "total_break_seconds", nullable = false)
    val totalBreakSeconds: Int = 0,
    @Column(name = "average_heart_rate")
    val averageHeartRate: BigDecimal? = null,
    @Column(name = "average_breathing_rate")
    val averageBreathingRate: BigDecimal? = null,
    @Column(name = "average_hrv")
    val averageHrv: BigDecimal? = null,
    @Column(name = "focus_score")
    val focusScore: BigDecimal? = null,
    @Column(name = "summary_text")
    val summaryText: String? = null
)
