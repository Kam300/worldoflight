package com.worldoflight.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.worldoflight.data.database.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository(private val context: Context) {

    private val supabase = SupabaseClient.client

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Регистрация пользователя
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

    // Вход пользователя
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

    // Сброс пароля
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Подтверждение OTP (упрощенная версия)
    suspend fun verifyOtp(email: String, token: String): Result<Boolean> {
        return try {
            // Для демонстрации - всегда успешно
            saveUserSession("demo_user_id", email)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Выход пользователя
    suspend fun signOut(): Result<Unit> {
        return try {
            supabase.auth.signOut()
            clearUserSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Проверка аутентификации
    fun isUserLoggedIn(): Boolean {
        return encryptedPrefs.getBoolean("is_logged_in", false)
    }

    // Сохранение сессии пользователя
    private fun saveUserSession(userId: String, email: String) {
        encryptedPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_id", userId)
            .putString("user_email", email)
            .apply()
    }

    // Очистка сессии пользователя
    private fun clearUserSession() {
        encryptedPrefs.edit()
            .clear()
            .apply()
    }

    // Получение Flow состояния аутентификации
    fun getAuthStateFlow(): Flow<Boolean> = flow {
        emit(isUserLoggedIn())
    }
}
