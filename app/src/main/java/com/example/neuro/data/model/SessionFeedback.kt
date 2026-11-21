package com.example.neuro.data.model

import com.google.gson.annotations.SerializedName

data class SessionFeedback(
    val id: Long = 0L,
    @SerializedName("user_id") val userId: Long = 1L,
    @SerializedName("session_id") val sessionId: Long,
    @SerializedName("focus_rating") val focusRating: Int, // 1 to 5
    @SerializedName("mood_rating") val moodRating: Int? = null, // 1 to 5
    @SerializedName("productivity_rating") val productivityRating: Int? = null, // 1 to 5
    @SerializedName("feedback_text") val feedbackText: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
