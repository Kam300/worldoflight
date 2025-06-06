package com.worldoflight.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val id: Long = 0,
    val product_id: Long = 0,
    val user_id: Long = 0,
    val quantity: Int = 1,
    val price: Double = 0.0,
    val created_at: String = "",
    val product: Product? = null
)
