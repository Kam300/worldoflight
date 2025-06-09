package com.worldoflight.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: Long = 0,
    @SerialName("user_id")
    val userId: String, // Должно быть String, не UUID
    @SerialName("order_number")
    val orderNumber: String,
    val status: String = "pending", // pending, confirmed, shipped, delivered
    @SerialName("total_amount")
    val totalAmount: Double,
    @SerialName("discount_amount")
    val discountAmount: Double = 0.0,
    @SerialName("delivery_fee")
    val deliveryFee: Double = 0.0,
    @SerialName("payment_method")
    val paymentMethod: String,
    @SerialName("delivery_address")
    val deliveryAddress: String,
    @SerialName("contact_phone")
    val contactPhone: String,
    @SerialName("contact_email")
    val contactEmail: String,
    @SerialName("promo_code")
    val promoCode: String? = null,
    @SerialName("order_items")
    val orderItems: List<OrderItem> = emptyList(),
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("estimated_delivery")
    val estimatedDelivery: String? = null
)

@Serializable
data class OrderItem(
    val id: Long = 0,
    @SerialName("order_id")
    val orderId: Long,
    @SerialName("product_id")
    val productId: Long,
    @SerialName("product_name")
    val productName: String,
    @SerialName("product_price")
    val productPrice: Double,
    val quantity: Int,
    @SerialName("total_price")
    val totalPrice: Double
)

@Serializable
data class CheckoutRequest(
    @SerialName("contact_email")
    val contactEmail: String,
    @SerialName("contact_phone")
    val contactPhone: String,
    @SerialName("delivery_address")
    val deliveryAddress: String,
    @SerialName("payment_method")
    val paymentMethod: String,
    @SerialName("cart_items")
    val cartItems: List<CartItem>,
    @SerialName("promo_code")
    val promoCode: String? = null
)
