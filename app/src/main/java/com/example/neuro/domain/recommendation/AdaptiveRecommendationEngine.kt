package com.example.neuro.domain.recommendation

import com.example.neuro.data.model.*
import java.text.SimpleDateFormat
import java.util.*

class AdaptiveRecommendationEngine {

    /**
     * Analyzes current session duration and physiological measurements to generate
     * contextual, non-medical adaptive productivity suggestions.
     */
    fun generateRecommendation(
        sessionId: Long,
        userId: Long,
        elapsedSeconds: Int,
        heartRate: Double,
        breathingRate: Double,
        hrv: Double
    ): Recommendation? {
        val nowStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        // 1. High Heart Rate / Elevated Tension -> Guided Breathing Exercise
        if (heartRate > 92.0 || hrv < 30.0) {
            return Recommendation(
                userId = userId,
                sessionId = sessionId,
                recommendationType = RecommendationType.BREATHING,
                title = "Try a 2-Minute Breathing Pause",
                message = "Your pulse rate indicates elevated intensity. Take 5 deep, slow breaths to restore calm focus.",
                reason = "Elevated heart rate (${heartRate.toInt()} bpm) detected",
                priority = Priority.HIGH,
                createdAt = nowStr
            )
        }

        // 2. Long Continuous Focus (>25 mins) -> Short Break Suggestion
        if (elapsedSeconds >= 1500 && elapsedSeconds % 300 == 0) { // At 25m mark
            return Recommendation(
                userId = userId,
                sessionId = sessionId,
                recommendationType = RecommendationType.BREAK,
                title = "Planned Focus Session Milestone",
                message = "Great work! You have completed 25 minutes. Consider taking a 5-minute break.",
                reason = "Scheduled Pomodoro target reached",
                priority = Priority.MEDIUM,
                createdAt = nowStr
            )
        }

        // 3. Extended Sitting / Shallow Breathing -> Movement / Hydration
        if (breathingRate > 20.0 || (elapsedSeconds > 2400 && elapsedSeconds % 600 == 0)) {
            return Recommendation(
                userId = userId,
                sessionId = sessionId,
                recommendationType = RecommendationType.MOVEMENT,
                title = "Hydration & Movement Prompt",
                message = "Stand up, stretch your back, and grab a cup of water to keep your mind clear.",
                reason = "Extended focus duration",
                priority = Priority.LOW,
                createdAt = nowStr
            )
        }

        return null
    }
}
