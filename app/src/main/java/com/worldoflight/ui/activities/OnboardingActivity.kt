package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.worldoflight.R
import com.worldoflight.data.models.OnboardingPage
import com.worldoflight.databinding.ActivityOnboardingBinding
import com.worldoflight.ui.adapters.OnboardingAdapter

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter
    private val onboardingPages = listOf(
        OnboardingPage(
            title = "Добро пожаловать в Мир Света",
            description = "Откройте для себя удивительный мир качественного освещения",
            imageRes = R.drawable.onboarding_welcome,
            backgroundColor = R.color.onboarding_blue
        ),
        OnboardingPage(
            title = "Начнем путешествие",
            description = "Умная, великолепная и модная коллекция. Изучите сейчас",
            imageRes = R.drawable.onboarding_journey,
            backgroundColor = R.color.onboarding_blue
        ),
        OnboardingPage(
            title = "У Вас Есть Сила, Чтобы",
            description = "В вашей комнате много красивых и привлекательных растений",
            imageRes = R.drawable.onboarding_power,
            backgroundColor = R.color.onboarding_blue
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOnboarding()
        setupClickListeners()
    }

    private fun setupOnboarding() {
        onboardingAdapter = OnboardingAdapter(onboardingPages)
        binding.viewPager.adapter = onboardingAdapter

        // Настройка индикаторов
        binding.dotsIndicator.attachTo(binding.viewPager)

        // Обработка изменения страниц
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateUI(position)
            }
        })

        updateUI(0)
    }

    private fun updateUI(position: Int) {
        val isLastPage = position == onboardingPages.size - 1

        binding.btnNext.text = if (isLastPage) "Начать" else "Далее"
        binding.btnSkip.visibility = if (isLastPage) View.INVISIBLE else View.VISIBLE

        // Изменяем цвет фона
        val backgroundColor = ContextCompat.getColor(this, onboardingPages[position].backgroundColor)
        binding.root.setBackgroundColor(backgroundColor)
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            val currentPosition = binding.viewPager.currentItem
            if (currentPosition < onboardingPages.size - 1) {
                binding.viewPager.currentItem = currentPosition + 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        // Сохраняем, что пользователь прошел онбординг
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()

        // ИСПРАВЛЕНО: Переходим к экрану авторизации
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()

        // Плавный переход
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
