package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString()

                if (validateInput(email, password)) {
                    performLogin(email, password)
                }
            }

            tvCreateAccount.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }

            tvForgotPassword.setOnClickListener {
                startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Введите email"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Неверный формат email"
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Введите пароль"
            return false
        }

        return true
    }

    private fun performLogin(email: String, password: String) {
        // TODO: Здесь будет логика аутентификации через Supabase
        // Пока что просто переходим в главное приложение
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
