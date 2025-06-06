package com.worldoflight.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.worldoflight.data.models.CartItem
import com.worldoflight.data.models.Product

object CartManager {

    private const val PREFS_NAME = "cart"
    private const val KEY_CART_ITEMS = "cart_items"
    private val gson = Gson()

    fun addToCart(context: Context, product: Product, quantity: Int = 1) {
        val currentItems = getCartItems(context).toMutableList()

        // Проверяем, есть ли уже такой товар в корзине
        val existingItemIndex = currentItems.indexOfFirst { it.product_id == product.id }

        if (existingItemIndex != -1) {
            // Увеличиваем количество существующего товара
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + quantity
            )
        } else {
            // Добавляем новый товар
            val newCartItem = CartItem(
                id = System.currentTimeMillis(), // Временный ID
                product_id = product.id,
                user_id = 1, // Временный user ID
                quantity = quantity,
                price = product.price,
                created_at = System.currentTimeMillis().toString(),
                product = product
            )
            currentItems.add(newCartItem)
        }

        saveCartItems(context, currentItems)
    }

    fun removeFromCart(context: Context, cartItemId: Long) {
        val currentItems = getCartItems(context).toMutableList()
        currentItems.removeAll { it.id == cartItemId }
        saveCartItems(context, currentItems)
    }

    fun updateQuantity(context: Context, cartItemId: Long, newQuantity: Int) {
        val currentItems = getCartItems(context).toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.id == cartItemId }

        if (itemIndex != -1) {
            if (newQuantity <= 0) {
                // Удаляем товар если количество 0 или меньше
                currentItems.removeAt(itemIndex)
            } else {
                // Обновляем количество
                val updatedItem = currentItems[itemIndex].copy(quantity = newQuantity)
                currentItems[itemIndex] = updatedItem
            }
            saveCartItems(context, currentItems)
        }
    }


    fun getCartItems(context: Context): List<CartItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cartJson = prefs.getString(KEY_CART_ITEMS, "[]")
        val type = object : TypeToken<List<CartItem>>() {}.type
        return gson.fromJson(cartJson, type) ?: emptyList()
    }

    fun getCartItemsCount(context: Context): Int {
        return getCartItems(context).sumOf { it.quantity }
    }

    fun getTotalPrice(context: Context): Double {
        return getCartItems(context).sumOf { it.price * it.quantity }
    }

    fun clearCart(context: Context) {
        saveCartItems(context, emptyList())
    }

    private fun saveCartItems(context: Context, items: List<CartItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CART_ITEMS, gson.toJson(items))
            .apply()
    }
}
