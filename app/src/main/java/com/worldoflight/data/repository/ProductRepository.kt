package com.worldoflight.data.repository

import com.worldoflight.data.models.Product
import com.worldoflight.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from

class ProductRepository {

    private val supabase = SupabaseClient.client

    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val products = supabase.from("products")
                .select {
                    filter {
                        eq("in_stock", true)
                        gt("stock_quantity", 0)
                    }
                }
                .decodeList<Product>()

            // Логирование для отладки
            products.forEach { product ->
                android.util.Log.d("ProductRepo", "Product: ${product.name}, ImageURL: ${product.image_url}")
            }

            Result.success(products)
        } catch (e: Exception) {
            android.util.Log.e("ProductRepo", "Error loading products", e)
            Result.failure(e)
        }
    }


    // Получение популярных продуктов с остатками
    suspend fun getPopularProducts(): Result<List<Product>> {
        return try {
            val products = supabase.from("products")
                .select {
                    filter {
                        eq("in_stock", true)
                        gt("stock_quantity", 0) // только товары с остатками
                    }
                    limit(10)
                }
                .decodeList<Product>()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Обновление остатков товара после покупки
    suspend fun updateProductStock(productId: Long, newQuantity: Int): Result<Boolean> {
        return try {
            supabase.from("products")
                .update({
                    set("stock_quantity", newQuantity)
                    set("in_stock", newQuantity > 0)
                }) {
                    filter {
                        eq("id", productId)
                    }
                }
            Result.success(true)
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
                        gt("stock_quantity", 0)
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
                        gt("stock_quantity", 0)
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
                        gt("stock_quantity", 0)
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
                        gt("stock_quantity", 0)
                    }
                }
                .decodeList<Product>()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}
