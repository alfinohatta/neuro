package com.example.neuro.domain.gamification

data class CognitiveLevelStatus(
    val levelNumber: Int,
    val levelTitle: String,
    val totalXp: Int,
    val xpForNextLevel: Int,
    val progressPercent: Float
)

object CognitiveLevelEngine {

    fun calculateLevel(totalFocusMinutes: Int, completedSessionsCount: Int): CognitiveLevelStatus {
        val xp = (totalFocusMinutes * 10) + (completedSessionsCount * 50) + 250
        val levelNumber = (xp / 500) + 1
        val xpInCurrentLevel = xp % 500
        val progressPercent = (xpInCurrentLevel / 500f).coerceIn(0f, 1f)

        val title = when (levelNumber) {
            1 -> "Novice Focused Practitioner"
            2 -> "Consistent Flow Practitioner"
            3 -> "Cognitive Resilience Specialist"
            4 -> "Vagal Tone Master"
            else -> "Elite Flow Master"
        }

        return CognitiveLevelStatus(
            levelNumber = levelNumber,
            levelTitle = title,
            totalXp = xp,
            xpForNextLevel = 500,
            progressPercent = progressPercent
        )
    }
}
