package com.example.neuro.domain.breathing

enum class BreathingCategory {
    PARASYMPATHETIC_RESET,
    TACTICAL_FOCUS,
    HRV_RESONANCE,
    DIAPHRAGMATIC_RELAXATION
}

enum class AnimationStyle {
    PULSATION_RING,
    BOX_PERIMETER,
    SINE_WAVE_OCEAN,
    DUAL_DIAPHRAGM
}

data class BreathingPhase(
    val name: String,
    val durationSeconds: Int,
    val instruction: String,
    val targetScale: Float
)

data class BreathingTechnique(
    val id: String,
    val title: String,
    val category: BreathingCategory,
    val instructor: String,
    val defaultDurationMinutes: Int,
    val description: String,
    val purpose: String,
    val animationStyle: AnimationStyle,
    val phases: List<BreathingPhase>,
    val targetBpm: Double,
    val benefitTags: List<String>
)

object BreathingRepository {
    val techniques = listOf(
        BreathingTechnique(
            id = "478_reset",
            title = "4 - 7 - 8 Parasympathetic Reset",
            category = BreathingCategory.PARASYMPATHETIC_RESET,
            instructor = "Dr. Ramina Williamson",
            defaultDurationMinutes = 10,
            description = "Natural tranquilizer for the nervous system that rapidly reduces cortisol and lowers elevated heart rate.",
            purpose = "Fast anxiety reduction, stress relief & pre-sleep relaxation",
            animationStyle = AnimationStyle.PULSATION_RING,
            phases = listOf(
                BreathingPhase("Inhale", 4, "Inhale deeply & quietly through your nose", 1.25f),
                BreathingPhase("Hold", 7, "Hold your breath calmly with a relaxed jaw", 1.25f),
                BreathingPhase("Exhale", 8, "Exhale completely through mouth with a gentle whoosh sound", 0.6f)
            ),
            targetBpm = 3.15,
            benefitTags = listOf("Stress Relief", "Sleep Readiness", "Vagus Stimulation")
        ),
        BreathingTechnique(
            id = "box_focus",
            title = "Box / Square Tactical Focus",
            category = BreathingCategory.TACTICAL_FOCUS,
            instructor = "Thomas Sattler",
            defaultDurationMinutes = 15,
            description = "Military-grade tactical breathing used by high-performance teams to maintain calm alertness under pressure.",
            purpose = "Enhanced cognitive focus, emotional regulation & mental clarity",
            animationStyle = AnimationStyle.BOX_PERIMETER,
            phases = listOf(
                BreathingPhase("Inhale", 4, "Inhale smoothly through your nose", 1.0f),
                BreathingPhase("Hold Top", 4, "Hold air in your lungs calmly", 1.0f),
                BreathingPhase("Exhale", 4, "Exhale evenly through your mouth", 0.6f),
                BreathingPhase("Hold Empty", 4, "Hold with lungs empty before next breath", 0.6f)
            ),
            targetBpm = 3.75,
            benefitTags = listOf("Deep Work", "Peak Alertness", "Calm Focus")
        ),
        BreathingTechnique(
            id = "resonance_hrv",
            title = "Resonance Coherence (HRV Max)",
            category = BreathingCategory.HRV_RESONANCE,
            instructor = "Jeremy Shimmer",
            defaultDurationMinutes = 20,
            description = "Paced breathing at ~5.5 seconds per phase (6 breaths/min) to synchronize heart rate variability with respiratory sinus arrhythmia.",
            purpose = "Vascular resonance optimization, HRV maximization & flow state entry",
            animationStyle = AnimationStyle.SINE_WAVE_OCEAN,
            phases = listOf(
                BreathingPhase("Inhale", 5, "Breathe in smooth, uninterrupted breath", 1.15f),
                BreathingPhase("Exhale", 5, "Release breath like a smooth ocean wave", 0.65f)
            ),
            targetBpm = 6.0,
            benefitTags = listOf("HRV Boost", "Flow State", "Cardiovascular Sync")
        ),
        BreathingTechnique(
            id = "diaphragmatic_belly",
            title = "Diaphragmatic Somatic Release",
            category = BreathingCategory.DIAPHRAGMATIC_RELAXATION,
            instructor = "Dr. Elena Rostova",
            defaultDurationMinutes = 10,
            description = "Deep abdominal breathing that engages the diaphragm muscle, expanding the belly rather than the chest.",
            purpose = "Somatic muscle tension release, chest tightness relief & oxygenation",
            animationStyle = AnimationStyle.DUAL_DIAPHRAGM,
            phases = listOf(
                BreathingPhase("Inhale (Belly)", 5, "Expand your abdomen outward like a balloon", 1.25f),
                BreathingPhase("Pause", 2, "Feel the expansive stillness in your core", 1.25f),
                BreathingPhase("Exhale (Soft)", 6, "Softly collapse your belly back toward your spine", 0.55f)
            ),
            targetBpm = 4.6,
            benefitTags = listOf("Tension Release", "Abdominal Support", "Full Oxygenation")
        )
    )

    fun getTechniqueById(id: String): BreathingTechnique {
        return techniques.find { it.id == id } ?: techniques.first()
    }
}
