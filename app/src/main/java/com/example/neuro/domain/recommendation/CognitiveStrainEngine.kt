package com.example.neuro.domain.recommendation

enum class StrainLevel {
    OPTIMAL,
    MODERATE,
    ELEVATED,
    FATIGUE_RISK
}

data class CognitiveStrainStatus(
    val strainScore: Int,
    val level: StrainLevel,
    val recommendationText: String,
    val peakFocusWindow: String = "9:00 AM – 11:30 AM"
)

class CognitiveStrainEngine {

    fun calculateStrain(
        elapsedMinutes: Int,
        heartRateBpm: Double,
        breathingRateBpm: Double,
        hrvMs: Double
    ): CognitiveStrainStatus {
        var score = (elapsedMinutes * 0.8).toInt()

        if (heartRateBpm > 85.0) score += 15
        if (hrvMs < 35.0) score += 20
        if (breathingRateBpm > 20.0) score += 10

        val finalScore = score.coerceIn(10, 95)

        val (level, text) = when {
            finalScore < 35 -> Pair(StrainLevel.OPTIMAL, "Optimal cognitive flow. High HRV resilience detected.")
            finalScore < 60 -> Pair(StrainLevel.MODERATE, "Steady focus effort. Maintain current pacing.")
            finalScore < 80 -> Pair(StrainLevel.ELEVATED, "Elevated cognitive strain. Consider a 5-minute parasympathetic break soon.")
            else -> Pair(StrainLevel.FATIGUE_RISK, "High fatigue risk! Rest recommended to prevent mental burnout.")
        }

        return CognitiveStrainStatus(
            strainScore = finalScore,
            level = level,
            recommendationText = text
        )
    }
}
