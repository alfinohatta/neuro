package com.example.neuro.data.model

import com.google.gson.annotations.SerializedName

data class UserPreferences(
    val id: Long = 1L,
    @SerializedName("user_id") val userId: Long = 1L,
    @SerializedName("focus_duration_minutes") val focusDurationMinutes: Int = 25,
    @SerializedName("break_duration_minutes") val breakDurationMinutes: Int = 5,
    @SerializedName("daily_focus_goal_minutes") val dailyFocusGoalMinutes: Int = 120,
    @SerializedName("notifications_enabled") val notificationsEnabled: Boolean = true,
    @SerializedName("sound_enabled") val soundEnabled: Boolean = true,
    @SerializedName("adaptive_recommendations_enabled") val adaptiveRecommendationsEnabled: Boolean = true,
    val theme: String = "system"
)
