package com.example.neuro.data.model

import com.google.gson.annotations.SerializedName

enum class SignalType {
    @SerializedName("heart_rate") HEART_RATE,
    @SerializedName("breathing_rate") BREATHING_RATE,
    @SerializedName("hrv") HRV,
    @SerializedName("movement") MOVEMENT,
    @SerializedName("facial_expression") FACIAL_EXPRESSION
}

enum class SignalQuality {
    @SerializedName("low") LOW,
    @SerializedName("medium") MEDIUM,
    @SerializedName("high") HIGH,
    @SerializedName("unknown") UNKNOWN
}

data class SignalMeasurement(
    val id: Long = 0L,
    @SerializedName("user_id") val userId: Long = 1L,
    @SerializedName("session_id") val sessionId: Long,
    val provider: String = "Presage SmartSpectra",
    @SerializedName("signal_type") val signalType: SignalType,
    @SerializedName("signal_value") val signalValue: Double,
    val unit: String,
    @SerializedName("signal_quality") val signalQuality: SignalQuality = SignalQuality.HIGH,
    @SerializedName("measured_at") val measuredAt: String,
    @SerializedName("expires_at") val expiresAt: String? = null
)
