package com.example.neuro.data.model

import com.google.gson.annotations.SerializedName

enum class RecommendationType {
    @SerializedName("focus") FOCUS,
    @SerializedName("break") BREAK,
    @SerializedName("breathing") BREATHING,
    @SerializedName("environment") ENVIRONMENT,
    @SerializedName("hydration") HYDRATION,
    @SerializedName("movement") MOVEMENT
}

enum class Priority {
    @SerializedName("low") LOW,
    @SerializedName("medium") MEDIUM,
    @SerializedName("high") HIGH
}

enum class RecommendationStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("accepted") ACCEPTED,
    @SerializedName("dismissed") DISMISSED,
    @SerializedName("completed") COMPLETED
}

data class Recommendation(
    val id: Long = 0L,
    @SerializedName("user_id") val userId: Long = 1L,
    @SerializedName("session_id") val sessionId: Long,
    @SerializedName("recommendation_type") val recommendationType: RecommendationType,
    val title: String,
    val message: String,
    val reason: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val status: RecommendationStatus = RecommendationStatus.PENDING,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("responded_at") val respondedAt: String? = null
)
