package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.databinding.ActivitySplashBinding
import com.worldoflight.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashTimeOut = 2500L // 2.5 секунды

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Анимация логотипа
        animateLogo()

        // Переход к следующему экрану
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, splashTimeOut)
    }

    private fun animateLogo() {
        binding.ivLogo.apply {
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f

            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1500)
                .start()
        }

        binding.tvAppName.apply {
            alpha = 0f
            translationY = 100f

            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1500)
                .setStartDelay(500)
                .start()
        }
    }

    private fun navigateToNextScreen() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)

        // Проверяем авторизацию
        val currentUser = SupabaseClient.client.auth.currentUserOrNull()

        val intent = when {
            // Если онбординг не пройден - показываем онбординг
            !onboardingCompleted -> Intent(this, OnboardingActivity::class.java)

            // Если онбординг пройден, но пользователь не авторизован - показываем логин
            currentUser == null -> Intent(this, LoginActivity::class.java)

            // Если пользователь авторизован - переходим в главное приложение
            else -> Intent(this, MainActivity::class.java)
        }

        startActivity(intent)
        finish()

        // Плавный переход
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
