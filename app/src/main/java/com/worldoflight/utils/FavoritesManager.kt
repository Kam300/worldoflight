package com.worldoflight.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.worldoflight.data.models.Product

object FavoritesManager {

    private const val PREFS_NAME = "favorites"
    private const val KEY_FAVORITE_PRODUCTS = "favorite_products"
    private val gson = Gson()

    fun addToFavorites(context: Context, product: Product) {
        val currentFavorites = getFavoriteProducts(context).toMutableList()

        // Проверяем, нет ли уже такого товара
        if (!currentFavorites.any { it.id == product.id }) {
            currentFavorites.add(product)
            saveFavoriteProducts(context, currentFavorites)
        }
    }

    fun removeFromFavorites(context: Context, product: Product) {
        val currentFavorites = getFavoriteProducts(context).toMutableList()
        currentFavorites.removeAll { it.id == product.id }
        saveFavoriteProducts(context, currentFavorites)
    }

    fun isFavorite(context: Context, productId: Long): Boolean {
        return getFavoriteProducts(context).any { it.id == productId }
    }

    fun getFavoriteProducts(context: Context): List<Product> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val favoritesJson = prefs.getString(KEY_FAVORITE_PRODUCTS, "[]")
        val type = object : TypeToken<List<Product>>() {}.type
        return gson.fromJson(favoritesJson, type) ?: emptyList()
    }

    private fun saveFavoriteProducts(context: Context, products: List<Product>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_FAVORITE_PRODUCTS, gson.toJson(products))
            .apply()
    }
}
