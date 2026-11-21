package com.example.neuro.data.model

import com.google.gson.annotations.SerializedName

enum class ConsentType {
    @SerializedName("camera") CAMERA,
    @SerializedName("signal_processing") SIGNAL_PROCESSING,
    @SerializedName("analytics") ANALYTICS,
    @SerializedName("privacy_policy") PRIVACY_POLICY
}

data class UserConsent(
    val id: Long = 0L,
    @SerializedName("user_id") val userId: Long = 1L,
    @SerializedName("consent_type") val consentType: ConsentType,
    @SerializedName("consent_version") val consentVersion: String = "1.0",
    @SerializedName("privacy_policy_version") val privacyPolicyVersion: String? = "1.0",
    @SerializedName("is_granted") val isGranted: Boolean = false,
    @SerializedName("granted_at") val grantedAt: String? = null,
    @SerializedName("revoked_at") val revokedAt: String? = null
)
