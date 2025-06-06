package com.worldoflight.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val avatar_url: String = "",
    val created_at: String = "",
    val updated_at: String = ""
)
