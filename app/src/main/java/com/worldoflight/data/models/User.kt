package com.worldoflight.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val name: String? = null,
    val surname: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val avatar_url: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val surname: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val avatar_url: String? = null,
    val updated_at: String? = null
)