package com.worldoflight.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: Long = 0,
    val title: String = "",
    val message: String = "",
    val type: String = "", // order, promotion, system
    val is_read: Boolean = false,
    val created_at: String = "",
    val user_id: Long = 0
)

enum class NotificationType(val value: String) {
    ORDER("order"),
    PROMOTION("promotion"),
    SYSTEM("system")
}
