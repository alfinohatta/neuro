package com.example.neuro.controller

import com.example.neuro.entity.*
import com.example.neuro.repository.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userRepository: UserRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val consentRepository: UserConsentRepository
) {
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserEntity> {
        val user = userRepository.findById(id)
        return if (user.isPresent) ResponseEntity.ok(user.get()) else ResponseEntity.notFound().build()
    }

    @GetMapping("/{id}/preferences")
    fun getPreferences(@PathVariable id: Long): ResponseEntity<UserPreferencesEntity> {
        val prefs = preferencesRepository.findByUserId(id)
        return if (prefs.isPresent) ResponseEntity.ok(prefs.get()) else ResponseEntity.ok(UserPreferencesEntity(userId = id))
    }

    @PutMapping("/{id}/preferences")
    fun updatePreferences(
        @PathVariable id: Long,
        @RequestBody preferences: UserPreferencesEntity
    ): ResponseEntity<UserPreferencesEntity> {
        val saved = preferencesRepository.save(preferences.copy(userId = id))
        return ResponseEntity.ok(saved)
    }

    @GetMapping("/{id}/consents")
    fun getConsents(@PathVariable id: Long): List<UserConsentEntity> {
        return consentRepository.findByUserId(id)
    }
}

@RestController
@RequestMapping("/api/sessions")
class FocusSessionController(
    private val sessionRepository: FocusSessionRepository,
    private val summaryRepository: SessionSummaryRepository
) {
    @GetMapping("/user/{userId}")
    fun getSessions(@PathVariable userId: Long): List<FocusSessionEntity> {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId)
    }

    @PostMapping
    fun createSession(@RequestBody session: FocusSessionEntity): FocusSessionEntity {
        return sessionRepository.save(session.copy(status = "active", startedAt = LocalDateTime.now()))
    }

    @PostMapping("/{id}/end")
    fun endSession(@PathVariable id: Long): ResponseEntity<FocusSessionEntity> {
        val sessionOpt = sessionRepository.findById(id)
        if (sessionOpt.isEmpty) return ResponseEntity.notFound().build()

        val session = sessionOpt.get().copy(
            status = "completed",
            endedAt = LocalDateTime.now()
        )
        return ResponseEntity.ok(sessionRepository.save(session))
    }

    @GetMapping("/{id}/summary")
    fun getSummary(@PathVariable id: Long): ResponseEntity<SessionSummaryEntity> {
        val summary = summaryRepository.findBySessionId(id)
        return if (summary.isPresent) ResponseEntity.ok(summary.get()) else ResponseEntity.notFound().build()
    }
}

@RestController
@RequestMapping("/api/signals")
class SignalMeasurementController(
    private val signalRepository: SignalMeasurementRepository
) {
    @PostMapping
    fun recordSignal(@RequestBody signal: SignalMeasurementEntity): SignalMeasurementEntity {
        return signalRepository.save(signal.copy(measuredAt = LocalDateTime.now()))
    }

    @GetMapping("/session/{sessionId}")
    fun getSignalsForSession(@PathVariable sessionId: Long): List<SignalMeasurementEntity> {
        return signalRepository.findBySessionIdOrderByMeasuredAtDesc(sessionId)
    }
}
