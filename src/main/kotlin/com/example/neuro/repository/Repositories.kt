package com.example.neuro.repository

import com.example.neuro.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): Optional<UserEntity>
}

@Repository
interface UserPreferencesRepository : JpaRepository<UserPreferencesEntity, Long> {
    fun findByUserId(userId: Long): Optional<UserPreferencesEntity>
}

@Repository
interface UserConsentRepository : JpaRepository<UserConsentEntity, Long> {
    fun findByUserId(userId: Long): List<UserConsentEntity>
}

@Repository
interface FocusSessionRepository : JpaRepository<FocusSessionEntity, Long> {
    fun findByUserIdOrderByStartedAtDesc(userId: Long): List<FocusSessionEntity>
}

@Repository
interface SignalMeasurementRepository : JpaRepository<SignalMeasurementEntity, Long> {
    fun findBySessionIdOrderByMeasuredAtDesc(sessionId: Long): List<SignalMeasurementEntity>
}

@Repository
interface RecommendationRepository : JpaRepository<RecommendationEntity, Long> {
    fun findBySessionId(sessionId: Long): List<RecommendationEntity>
}

@Repository
interface SessionFeedbackRepository : JpaRepository<SessionFeedbackEntity, Long> {
    fun findBySessionId(sessionId: Long): Optional<SessionFeedbackEntity>
}

@Repository
interface SessionSummaryRepository : JpaRepository<SessionSummaryEntity, Long> {
    fun findBySessionId(sessionId: Long): Optional<SessionSummaryEntity>
}
