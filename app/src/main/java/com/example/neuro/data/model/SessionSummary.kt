package com.example.neuro.data.model

import com.google.gson.annotations.SerializedName

data class SessionSummary(
    val id: Long = 0L,
    @SerializedName("user_id") val userId: Long = 1L,
    @SerializedName("session_id") val sessionId: Long,
    @SerializedName("total_focus_seconds") val totalFocusSeconds: Int = 0,
    @SerializedName("total_break_seconds") val totalBreakSeconds: Int = 0,
    @SerializedName("average_heart_rate") val averageHeartRate: Double? = null,
    @SerializedName("average_breathing_rate") val averageBreathingRate: Double? = null,
    @SerializedName("average_hrv") val averageHrv: Double? = null,
    @SerializedName("focus_score") val focusScore: Double? = null, // 0 to 100
    @SerializedName("summary_text") val summaryText: String? = null,
    @SerializedName("generated_at") val generatedAt: String? = null
)
