package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.databinding.ActivityNewPasswordBinding
import com.worldoflight.ui.viewmodels.AuthState
import com.worldoflight.ui.viewmodels.AuthViewModel

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewPasswordBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email") ?: ""

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.PasswordUpdated -> {
                    android.widget.Toast.makeText(this, "Пароль успешно обновлен", android.widget.Toast.LENGTH_LONG).show()
                    // Переход на экран входа
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
                else -> {
                    // Обрабатываем другие состояния
                }
            }
        }

        authViewModel.isLoading.observe(this) { isLoading ->
            binding.btnUpdatePassword.isEnabled = !isLoading
            binding.btnUpdatePassword.text = if (isLoading) "Обновление..." else "Обновить пароль"
        }

        authViewModel.errorMessage.observe(this) { error ->
            error?.let {
                android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_LONG).show()
                authViewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            btnUpdatePassword.setOnClickListener {
                val password = etPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (validatePasswords(password, confirmPassword)) {
                    authViewModel.updatePassword(password)
                }
            }
        }
    }

    private fun validatePasswords(password: String, confirmPassword: String): Boolean {
        if (password.isEmpty()) {
            binding.etPassword.error = "Введите новый пароль"
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Пароль должен содержать минимум 6 символов"
            return false
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Подтвердите пароль"
            return false
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Пароли не совпадают"
            return false
        }

        return true
    }
}
