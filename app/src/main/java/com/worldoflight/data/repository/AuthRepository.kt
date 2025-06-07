package com.worldoflight.data.repository

import android.content.Context
import android.content.SharedPreferences
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

    // Регистрация пользователя с OTP
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

    // Подтверждение OTP кода
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

    // ИСПРАВЛЕННЫЙ метод получения профиля
    suspend fun getUserProfile(): Result<UserProfile?> {
        return try {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                val profile = supabase.from("profiles")
                    .select {
                        filter {
                            eq("id", user.id)  // ✅ eq внутри filter блока
                        }
                    }
                    .decodeSingle<UserProfile>()
                Result.success(profile)
            } else {
                Result.failure(Exception("Пользователь не авторизован"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // ИСПРАВЛЕННЫЙ метод обновления профиля
    suspend fun updateUserProfile(updateRequest: UpdateProfileRequest): Result<UserProfile> {
        return try {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                // Обновляем профиль
                supabase.from("profiles")
                    .update(updateRequest) {
                        filter {
                            eq("id", user.id)  // ✅ eq внутри filter блока
                        }
                    }

                // Получаем обновленный профиль
                val updatedProfile = supabase.from("profiles")
                    .select {
                        filter {
                            eq("id", user.id)  // ✅ eq внутри filter блока
                        }
                    }
                    .decodeSingle<UserProfile>()

                Result.success(updatedProfile)
            } else {
                Result.failure(Exception("Пользователь не авторизован"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    // ПОЛНАЯ версия с Supabase Storage
    suspend fun uploadAvatar(imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
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
}
