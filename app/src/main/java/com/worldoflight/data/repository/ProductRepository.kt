package com.worldoflight.data.repository

import com.worldoflight.data.models.Product
import com.worldoflight.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from

class ProductRepository {

    private val supabase = SupabaseClient.client

    // Получение всех продуктов
    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val products = supabase.from("products")
                .select {
                    filter {
                        eq("in_stock", true)
                    }
                }
                .decodeList<Product>()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Получение популярных продуктов (первые 10)
    suspend fun getPopularProducts(): Result<List<Product>> {
        return try {
            val products = supabase.from("products")
                .select {
                    filter {
                        eq("in_stock", true)
                    }
                    limit(10)
                }
                .decodeList<Product>()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Получение продуктов по категории
    suspend fun getProductsByCategory(category: String): Result<List<Product>> {
        return try {
            val products = supabase.from("products")
                .select {
                    filter {
                        eq("in_stock", true)
                        eq("category", category)
                    }
                }
                .decodeList<Product>()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Получение продукта по ID
    suspend fun getProductById(productId: Long): Result<Product?> {
        return try {
            val product = supabase.from("products")
                .select {
                    filter {
                        eq("id", productId)
                        eq("in_stock", true)
                    }
                }
                .decodeSingle<Product>()
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Поиск продуктов по названию
    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val products = supabase.from("products")
                .select {
                    filter {
                        eq("in_stock", true)
                        ilike("name", "%$query%")
                    }
                }
                .decodeList<Product>()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Получение продуктов по бренду
    suspend fun getProductsByBrand(brand: String): Result<List<Product>> {
        return try {
            val products = supabase.from("products")
                .select {
                    filter {
                        eq("in_stock", true)
                        eq("brand", brand)
                    }
                }
                .decodeList<Product>()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Получение продуктов в ценовом диапазоне
    suspend fun getProductsByPriceRange(minPrice: Double, maxPrice: Double): Result<List<Product>> {
        return try {
            val products = supabase.from("products")
                .select {
                    filter {
                        eq("in_stock", true)
                        gte("price", minPrice)
                        lte("price", maxPrice)
                    }
                }
                .decodeList<Product>()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
