package com.worldoflight.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.worldoflight.data.models.UpdateProfileRequest
import com.worldoflight.data.models.UserProfile
import com.worldoflight.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

class AuthRepository(private val context: Context) {

    private val supabase = SupabaseClient.client

    // КЭШИРОВАНИЕ ПРОФИЛЯ
    private var cachedProfile: UserProfile? = null
    private var lastCacheTime: Long = 0
    private val cacheValidityMs = 5 * 60 * 1000L // 5 минут

    private val encryptedPrefs by lazy {
        createEncryptedPreferences()
    }

    private fun createEncryptedPreferences(): SharedPreferences? {
        return try {
            val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "auth_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to create EncryptedSharedPreferences", e)
            clearCorruptedData()
            context.getSharedPreferences("auth_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    private fun clearCorruptedData() {
        try {
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            val encryptedFile = File(prefsDir, "auth_prefs.xml")
            if (encryptedFile.exists()) {
                encryptedFile.delete()
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to clear corrupted data", e)
        }
    }

    // КЭШИРОВАННЫЙ метод получения профиля
    suspend fun getUserProfile(): Result<UserProfile?> {
        // Проверяем кэш
        val currentTime = System.currentTimeMillis()
        if (cachedProfile != null && (currentTime - lastCacheTime) < cacheValidityMs) {
            Log.d("AuthRepository", "Returning cached profile")
            return Result.success(cachedProfile)
        }

        return try {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                val profile = retryWithBackoff(maxRetries = 3) {
                    supabase.from("profiles")
                        .select {
                            filter {
                                eq("id", user.id)
                            }
                        }
                        .decodeSingle<UserProfile>()
                }

                // Обновляем кэш
                cachedProfile = profile
                lastCacheTime = currentTime
                Log.d("AuthRepository", "Profile loaded and cached")

                Result.success(profile)
            } else {
                Result.failure(Exception("Пользователь не авторизован"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error loading profile", e)

            // Возвращаем кэшированные данные при ошибке
            cachedProfile?.let {
                Log.d("AuthRepository", "Returning stale cache due to error")
                return Result.success(it)
            }

            Result.failure(e)
        }
    }

    // ОБНОВЛЕНИЕ профиля с очисткой кэша
    suspend fun updateUserProfile(updateRequest: UpdateProfileRequest): Result<UserProfile> {
        return try {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                // ОЧИЩАЕМ КЭШ ПЕРЕД ОБНОВЛЕНИЕМ
                clearProfileCache()
                Log.d("AuthRepository", "Cache cleared before profile update")

                // Обновляем профиль
                retryWithBackoff {
                    supabase.from("profiles")
                        .update(updateRequest) {
                            filter {
                                eq("id", user.id)
                            }
                        }
                }

                // Получаем обновленный профиль
                val updatedProfile = retryWithBackoff {
                    supabase.from("profiles")
                        .select {
                            filter {
                                eq("id", user.id)
                            }
                        }
                        .decodeSingle<UserProfile>()
                }

                // Обновляем кэш новыми данными
                cachedProfile = updatedProfile
                lastCacheTime = System.currentTimeMillis()
                Log.d("AuthRepository", "Profile updated and cache refreshed")

                Result.success(updatedProfile)
            } else {
                Result.failure(Exception("Пользователь не авторизован"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating profile", e)
            Result.failure(e)
        }
    }

    // ЗАГРУЗКА аватара с очисткой кэша
    suspend fun uploadAvatar(imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                // ОЧИЩАЕМ КЭШ ПЕРЕД ЗАГРУЗКОЙ АВАТАРА
                clearProfileCache()
                Log.d("AuthRepository", "Cache cleared before avatar upload")

                val bucket = supabase.storage.from("avatars")
                val path = "${user.id}/$fileName"

                // Удаляем старый файл если существует
                try {
                    bucket.delete(path)
                } catch (e: Exception) {
                    // Игнорируем ошибку если файл не существует
                }

                // Загружаем новый файл
                bucket.upload(path, imageBytes, upsert = true)

                // Получаем публичный URL
                val publicUrl = bucket.publicUrl(path)

                Result.success(publicUrl)
            } else {
                Result.failure(Exception("Пользователь не авторизован"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ВЫХОД с полной очисткой кэша
    suspend fun signOut(): Result<Unit> {
        return try {
            // ОЧИЩАЕМ ВСЕ КЭШИ ПЕРЕД ВЫХОДОМ
            clearAllCaches()
            Log.d("AuthRepository", "All caches cleared before logout")

            supabase.auth.signOut()
            clearUserSession()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Утилиты для управления кэшем
    fun clearProfileCache() {
        cachedProfile = null
        lastCacheTime = 0
        Log.d("AuthRepository", "Profile cache cleared")
    }

    fun clearAllCaches() {
        // Очищаем кэш профиля
        clearProfileCache()

        // Очищаем кэш изображений (Glide)
        try {
            com.bumptech.glide.Glide.get(context).clearMemory()
            Thread {
                com.bumptech.glide.Glide.get(context).clearDiskCache()
            }.start()
            Log.d("AuthRepository", "Glide cache cleared")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error clearing Glide cache", e)
        }

        // Очищаем SharedPreferences кэш
        try {
            val cachePrefs = context.getSharedPreferences("cache_prefs", Context.MODE_PRIVATE)
            cachePrefs.edit().clear().apply()
            Log.d("AuthRepository", "SharedPreferences cache cleared")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error clearing SharedPreferences cache", e)
        }
    }

    // Принудительное обновление кэша
    suspend fun forceRefreshProfile(): Result<UserProfile?> {
        clearProfileCache()
        return getUserProfile()
    }

    // Утилита для повторных попыток
    private suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        repeat(maxRetries - 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                Log.w("AuthRepository", "Attempt ${attempt + 1} failed: ${e.message}")
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
        return block() // Последняя попытка
    }

    // Остальные методы остаются без изменений...
    suspend fun signUp(email: String, password: String, name: String): Result<Boolean> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("name", name)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(email: String, token: String): Result<Boolean> {
        return try {
            supabase.auth.verifyEmailOtp(
                type = OtpType.Email.SIGNUP,
                email = email,
                token = token
            )

            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                saveUserSession(user.id, email)
                Result.success(true)
            } else {
                Result.failure(Exception("Ошибка верификации"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<Boolean> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                saveUserSession(user.id, email)
                Result.success(true)
            } else {
                Result.failure(Exception("Ошибка входа"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isUserLoggedIn(): Boolean {
        return try {
            encryptedPrefs?.getBoolean("is_logged_in", false) ?: false
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error checking login status", e)
            false
        }
    }

    private fun saveUserSession(userId: String, email: String) {
        encryptedPrefs?.edit()
            ?.putBoolean("is_logged_in", true)
            ?.putString("user_id", userId)
            ?.putString("user_email", email)
            ?.apply()
    }

    private fun clearUserSession() {
        encryptedPrefs?.edit()
            ?.clear()
            ?.apply()
    }
    // Сброс пароля
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Подтверждение OTP для сброса пароля
    suspend fun verifyPasswordResetOtp(email: String, token: String): Result<Boolean> {
        return try {
            supabase.auth.verifyEmailOtp(
                type = OtpType.Email.RECOVERY,
                email = email,
                token = token
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Повторная отправка OTP кода
    suspend fun resendOtp(email: String): Result<Boolean> {
        return try {
            supabase.auth.resendEmail(
                type = OtpType.Email.SIGNUP,
                email = email
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Установка нового пароля
    suspend fun updatePassword(newPassword: String): Result<Boolean> {
        return try {
            supabase.auth.updateUser {
                password = newPassword
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Повторная отправка для сброса пароля
    suspend fun resendPasswordResetOtp(email: String): Result<Boolean> {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
