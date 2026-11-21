package com.example.neuro.data.model

import com.google.gson.annotations.SerializedName

enum class SessionType {
    @SerializedName("focus") FOCUS,
    @SerializedName("break") BREAK,
    @SerializedName("deep_work") DEEP_WORK
}

enum class SessionStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("active") ACTIVE,
    @SerializedName("completed") COMPLETED,
    @SerializedName("cancelled") CANCELLED,
    @SerializedName("interrupted") INTERRUPTED
}

data class FocusSession(
    val id: Long = 0L,
    @SerializedName("user_id") val userId: Long = 1L,
    val title: String,
    val description: String? = null,
    @SerializedName("session_type") val sessionType: SessionType = SessionType.FOCUS,
    @SerializedName("planned_duration_minutes") val plannedDurationMinutes: Int = 25,
    @SerializedName("actual_duration_seconds") val actualDurationSeconds: Int = 0,
    val status: SessionStatus = SessionStatus.PENDING,
    @SerializedName("started_at") val startedAt: String? = null,
    @SerializedName("ended_at") val endedAt: String? = null,
    @SerializedName("interruption_count") val interruptionCount: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)
