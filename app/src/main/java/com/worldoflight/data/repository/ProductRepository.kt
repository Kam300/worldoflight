package com.worldoflight.data.repository

import com.worldoflight.data.models.Product
import com.worldoflight.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProductRepository {

    private val supabase = SupabaseClient.client
    // Кэш для продуктов
    private val productCache = mutableMapOf<String, CacheEntry<List<Product>>>()
    private val singleProductCache = mutableMapOf<Long, CacheEntry<Product>>()
    private val cacheMutex = Mutex()

    private val cacheValidityMs = 5 * 60 * 1000L // 5 минут
    private val maxCacheSize = 50 // Максимум 50 записей в кэше


    data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isValid(validityMs: Long): Boolean {
            return System.currentTimeMillis() - timestamp < validityMs
        }
    }


    suspend fun getAllProducts(): Result<List<Product>> {
        val cacheKey = "all_products"

        return cacheMutex.withLock {
            // Проверяем кэш
            productCache[cacheKey]?.let { cachedEntry ->
                if (cachedEntry.isValid(cacheValidityMs)) {
                    android.util.Log.d("ProductRepo", "Returning cached products")
                    return Result.success(cachedEntry.data)
                }
            }

            // Загружаем из БД
            try {
                val products = supabase.from("products")
                    .select {
                        filter {
                            eq("in_stock", true)
                            gt("stock_quantity", 0)
                        }
                    }
                    .decodeList<Product>()

                // Сохраняем в кэш
                productCache[cacheKey] = CacheEntry(products)
                cleanupCache()

                android.util.Log.d("ProductRepo", "Loaded ${products.size} products from DB")
                Result.success(products)
            } catch (e: Exception) {
                android.util.Log.e("ProductRepo", "Error loading products", e)

                // Возвращаем устаревший кэш при ошибке
                productCache[cacheKey]?.let {
                    android.util.Log.d("ProductRepo", "Returning stale cache due to error")
                    return Result.success(it.data)
                }

                Result.failure(e)
            }
        }
    }


    // Получение популярных продуктов с остатками
    suspend fun getPopularProducts(): Result<List<Product>> {
        val cacheKey = "popular_products"

        return cacheMutex.withLock {
            productCache[cacheKey]?.let { cachedEntry ->
                if (cachedEntry.isValid(cacheValidityMs)) {
                    return Result.success(cachedEntry.data)
                }
            }

            try {
                val products = supabase.from("products")
                    .select {
                        filter {
                            eq("in_stock", true)
                            gt("stock_quantity", 0)
                        }
                        limit(10)
                    }
                    .decodeList<Product>()

                productCache[cacheKey] = CacheEntry(products)
                cleanupCache()
                Result.success(products)
            } catch (e: Exception) {
                productCache[cacheKey]?.let {
                    return Result.success(it.data)
                }
                Result.failure(e)
            }
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

            // Инвалидируем кэш после обновления
            invalidateCache()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Методы управления кэшем
    private fun cleanupCache() {
        if (productCache.size > maxCacheSize) {
            val oldestKey = productCache.entries
                .minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { productCache.remove(it) }
        }
    }

    private fun cleanupSingleProductCache() {
        if (singleProductCache.size > maxCacheSize) {
            val oldestKey = singleProductCache.entries
                .minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { singleProductCache.remove(it) }
        }
    }

    suspend fun invalidateCache() {
        cacheMutex.withLock {
            productCache.clear()
            singleProductCache.clear()
            android.util.Log.d("ProductRepo", "Cache invalidated")
        }
    }

    suspend fun invalidateCacheForCategory(category: String) {
        cacheMutex.withLock {
            productCache.remove("category_$category")
            productCache.remove("all_products") // Также инвалидируем общий кэш
        }
    }
    // Предварительная загрузка кэша
    suspend fun preloadCache() {
        android.util.Log.d("ProductRepo", "Preloading cache...")
        getAllProducts()
        getPopularProducts()
    }

    // Получение статистики кэша
    fun getCacheStats(): String {
        return "Product cache: ${productCache.size} entries, Single product cache: ${singleProductCache.size} entries"
    }



    // Получение продуктов по категории
    suspend fun getProductsByCategory(category: String): Result<List<Product>> {
        val cacheKey = "category_$category"

        return cacheMutex.withLock {
            productCache[cacheKey]?.let { cachedEntry ->
                if (cachedEntry.isValid(cacheValidityMs)) {
                    return Result.success(cachedEntry.data)
                }
            }

            try {
                val products = supabase.from("products")
                    .select {
                        filter {
                            eq("in_stock", true)
                            eq("category", category)
                            gt("stock_quantity", 0)
                        }
                    }
                    .decodeList<Product>()

                productCache[cacheKey] = CacheEntry(products)
                cleanupCache()
                Result.success(products)
            } catch (e: Exception) {
                productCache[cacheKey]?.let {
                    return Result.success(it.data)
                }
                Result.failure(e)
            }
        }
    }

    // Получение продукта по ID
    suspend fun getProductById(productId: Long): Result<Product?> {
        return cacheMutex.withLock {
            // Проверяем кэш одиночных продуктов
            singleProductCache[productId]?.let { cachedEntry ->
                if (cachedEntry.isValid(cacheValidityMs)) {
                    return Result.success(cachedEntry.data)
                }
            }

            try {
                val product = supabase.from("products")
                    .select {
                        filter {
                            eq("id", productId)
                        }
                    }
                    .decodeSingle<Product>()

                singleProductCache[productId] = CacheEntry(product)
                cleanupSingleProductCache()
                Result.success(product)
            } catch (e: Exception) {
                singleProductCache[productId]?.let {
                    return Result.success(it.data)
                }
                Result.failure(e)
            }
        }
    }



    // Получение продуктов по бренду
    suspend fun getProductsByBrand(brand: String): Result<List<Product>> {
        val cacheKey = "brand_$brand"

        return cacheMutex.withLock {
            productCache[cacheKey]?.let { cachedEntry ->
                if (cachedEntry.isValid(cacheValidityMs)) {
                    return Result.success(cachedEntry.data)
                }
            }

            try {
                val products = supabase.from("products")
                    .select {
                        filter {
                            eq("in_stock", true)
                            eq("brand", brand)
                            gt("stock_quantity", 0)
                        }
                    }
                    .decodeList<Product>()

                productCache[cacheKey] = CacheEntry(products)
                cleanupCache()
                Result.success(products)
            } catch (e: Exception) {
                productCache[cacheKey]?.let {
                    return Result.success(it.data)
                }
                Result.failure(e)
            }
        }
    }

    // Получение продуктов в ценовом диапазоне
    suspend fun getProductsByPriceRange(minPrice: Double, maxPrice: Double): Result<List<Product>> {
        val cacheKey = "price_${minPrice}_$maxPrice"

        return cacheMutex.withLock {
            productCache[cacheKey]?.let { cachedEntry ->
                if (cachedEntry.isValid(cacheValidityMs)) {
                    return Result.success(cachedEntry.data)
                }
            }

            try {
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

                productCache[cacheKey] = CacheEntry(products)
                cleanupCache()
                Result.success(products)
            } catch (e: Exception) {
                productCache[cacheKey]?.let {
                    return Result.success(it.data)
                }
                Result.failure(e)
            }
        }
    }

    suspend fun searchProducts(query: String): Result<List<Product>> {
        // Поиск не кэшируем, так как запросы могут быть очень разнообразными
        return try {
            val products = supabase.from("products")
                .select {
                    filter {
                        or {
                            ilike("name", "%$query%")
                            ilike("description", "%$query%")
                            ilike("category", "%$query%")
                        }
                        eq("in_stock", true)
                        gt("stock_quantity", 0)
                    }
                }
                .decodeList<Product>()
            Result.success(products)
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Search error", e)
            Result.failure(e)
        }
    }



}
