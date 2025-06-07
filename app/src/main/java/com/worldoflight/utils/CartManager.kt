package com.worldoflight.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.worldoflight.data.models.Product
import com.worldoflight.data.models.CartItem

object CartManager {
    private const val PREFS_NAME = "cart_prefs"
    private const val CART_KEY = "cart_items"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun addToCart(context: Context, product: Product, quantity: Int): Boolean {
        val cartItems = getCartItems(context).toMutableList()

        val existingItem = cartItems.find { it.product?.id == product.id }
        val currentQuantityInCart = existingItem?.quantity ?: 0
        val totalQuantity = currentQuantityInCart + quantity

        // Проверяем остатки на складе
        if (totalQuantity > product.stock_quantity) {
            return false
        }

        if (existingItem != null) {
            val updatedItem = existingItem.copy(
                quantity = totalQuantity,
                price = product.price
            )
            val index = cartItems.indexOf(existingItem)
            cartItems[index] = updatedItem
        } else {
            val newItem = CartItem(
                id = System.currentTimeMillis(),
                product_id = product.id,
                quantity = quantity,
                price = product.price,
                product = product
            )
            cartItems.add(newItem)
        }

        saveCartItems(context, cartItems)
        return true
    }

    fun updateQuantity(context: Context, productId: Long, newQuantity: Int): Boolean {
        val cartItems = getCartItems(context).toMutableList()
        val itemIndex = cartItems.indexOfFirst { it.product?.id == productId }

        if (itemIndex != -1) {
            val item = cartItems[itemIndex]

            if (newQuantity <= 0) {
                // Удаляем товар если количество 0 или меньше
                cartItems.removeAt(itemIndex)
            } else {
                // Проверяем остатки только при увеличении
                val product = item.product
                if (product != null && newQuantity > item.quantity && newQuantity > product.stock_quantity) {
                    return false
                }
                cartItems[itemIndex] = item.copy(quantity = newQuantity)
            }
            saveCartItems(context, cartItems)
            return true
        }
        return false
    }

    fun removeFromCart(context: Context, productId: Long) {
        val cartItems = getCartItems(context).toMutableList()
        cartItems.removeAll { it.product?.id == productId }
        saveCartItems(context, cartItems)
    }

    fun getCartItems(context: Context): List<CartItem> {
        return try {
            val json = getPrefs(context).getString(CART_KEY, "[]")
            val type = object : TypeToken<List<CartItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCartItemsCount(context: Context): Int {
        return getCartItems(context).sumOf { it.quantity }
    }

    fun getCartTotal(context: Context): Double {
        return getCartItems(context).sumOf { (it.product?.price ?: it.price) * it.quantity }
    }

    fun clearCart(context: Context) {
        getPrefs(context).edit().remove(CART_KEY).apply()
    }

    private fun saveCartItems(context: Context, items: List<CartItem>) {
        val json = gson.toJson(items)
        getPrefs(context).edit().putString(CART_KEY, json).apply()
    }
}
