package com.worldoflight.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val image_url: String? = null, // Это поле для URL изображений
    val in_stock: Boolean = true,
    val brand: String = "",
    val power: String = "",
    val color_temperature: String = "",
    val created_at: String = "",
    val stock_quantity : Int = 0, // Количество на складе
) {
    // Вычисляемые свойства для совместимости с UI
    val formattedPrice: String
        get() = "₽${String.format("%.0f", price)}.00"

    val categoryDisplayName: String
        get() = when (category) {
            "bulbs" -> "Лампочки"
            "chandeliers" -> "Люстры"
            "floor_lamps" -> "Торшеры"
            "table_lamps" -> "Настольные лампы"
            "wall_lamps" -> "Бра"
            "led_strips" -> "LED ленты"
            else -> category
        }
}

@Serializable
data class Category(
    val id: Long = 0,
    val name: String = "",
    val display_name: String = "",
    val description: String = "",
    val icon_name: String = "",
    val image_url: String = "",
    val is_active: Boolean = true,
    val sort_order: Int = 0,
    val created_at: String = "",
    val updated_at: String = ""
)

enum class ProductCategory(val key: String, val displayName: String) {
    BULBS("bulbs", "Лампочки"),
    CHANDELIERS("chandeliers", "Люстры"),
    FLOOR_LAMPS("floor_lamps", "Торшеры"),
    TABLE_LAMPS("table_lamps", "Настольные лампы"),
    WALL_LAMPS("wall_lamps", "Бра"),
    LED_STRIPS("led_strips", "LED ленты")
}
