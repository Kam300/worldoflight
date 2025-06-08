package com.worldoflight.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Promotion(
    val id: Long,
    val title: String,
    val description: String? = null,
    @SerialName("promo_code")
    val promoCode: String,
    @SerialName("discount_type")
    val discountType: String, // "percentage" или "fixed_amount"
    @SerialName("discount_value")
    val discountValue: Double,
    @SerialName("min_order_amount")
    val minOrderAmount: Double = 0.0,
    @SerialName("max_discount_amount")
    val maxDiscountAmount: Double? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    @SerialName("usage_limit")
    val usageLimit: Int? = null,
    @SerialName("used_count")
    val usedCount: Int = 0,
    @SerialName("is_active")
    val isActive: Boolean = true
) {
    fun getDiscountText(): String {
        return when (discountType) {
            "percentage" -> "${discountValue.toInt()}%"
            "fixed_amount" -> "${discountValue.toInt()}₽"
            else -> ""
        }
    }

    fun isValid(): Boolean {
        // Проверка на активность и лимиты использования
        return isActive && (usageLimit == null || usedCount < usageLimit)
    }
}
