package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.databinding.ActivityRegisterBinding
import com.worldoflight.ui.viewmodels.AuthState
import com.worldoflight.ui.viewmodels.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.AwaitingVerification -> {
                    startActivity(Intent(this, VerificationActivity::class.java).apply {
                        putExtra("email", state.email)
                    })
                }
                is AuthState.Authenticated -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
                else -> {
                    // Обрабатываем другие состояния
                }
            }
        }

        authViewModel.isLoading.observe(this) { isLoading ->
            binding.btnRegister.isEnabled = !isLoading
            binding.btnRegister.text = if (isLoading) "Регистрация..." else "Зарегистрироваться"
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

            btnRegister.setOnClickListener {
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString()

                if (validateInput(name, email, password)) {
                    authViewModel.signUp(email, password, name)
                }
            }

            tvHaveAccount.setOnClickListener {
                finish()
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            binding.etName.error = "Введите имя"
            return false
        }

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

        if (password.length < 6) {
            binding.etPassword.error = "Пароль должен содержать минимум 6 символов"
            return false
        }

        if (!binding.cbAgree.isChecked) {
            android.widget.Toast.makeText(this, "Необходимо согласиться с условиями", android.widget.Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
