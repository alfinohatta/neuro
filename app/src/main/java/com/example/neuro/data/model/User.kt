package com.example.neuro.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long = 1L,
    @SerializedName("full_name") val fullName: String = "Alfino",
    val email: String = "alfino@example.com",
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val timezone: String = "UTC",
    @SerializedName("account_status") val accountStatus: String = "active",
    @SerializedName("email_verified") val emailVerified: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)
