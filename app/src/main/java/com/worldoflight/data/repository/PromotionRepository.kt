package com.worldoflight.data.repository

import com.worldoflight.data.models.Promotion
import com.worldoflight.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from

class PromotionRepository {

    private val supabase = SupabaseClient.client

    // Получение всех активных акций
    suspend fun getActivePromotions(): Result<List<Promotion>> {
        return try {
            val promotions = supabase.from("promotions")
                .select()
                .decodeList<Promotion>()
            Result.success(promotions)
        } catch (e: Exception) {
            android.util.Log.e("PromotionRepository", "Error loading promotions", e)
            Result.failure(e)
        }
    }

    // Получение акции по промокоду
    suspend fun getPromotionByCode(promoCode: String): Result<Promotion?> {
        return try {
            val promotions = supabase.from("promotions")
                .select()
                .decodeList<Promotion>()

            val promotion = promotions.find {
                it.promoCode.equals(promoCode, ignoreCase = true) && it.isActive
            }
            Result.success(promotion)
        } catch (e: Exception) {
            android.util.Log.e("PromotionRepository", "Error finding promotion by code", e)
            Result.failure(e)
        }
    }

    // Применение промокода (увеличение счетчика использования)
    suspend fun usePromoCode(promoId: Long): Result<Boolean> {
        return try {
            // Пока что возвращаем успех без реального обновления
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
