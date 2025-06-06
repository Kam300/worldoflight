package com.worldoflight.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val image_url: String = "",
    val in_stock: Boolean = true,
    val brand: String = "",
    val power: String = "",
    val color_temperature: String = "",
    val created_at: String = ""
)

enum class ProductCategory(val displayName: String) {
    BULBS("Лампочки"),
    CHANDELIERS("Люстры"),
    FLOOR_LAMPS("Торшеры"),
    TABLE_LAMPS("Настольные лампы"),
    WALL_LAMPS("Бра"),
    LED_STRIPS("LED ленты")
}

