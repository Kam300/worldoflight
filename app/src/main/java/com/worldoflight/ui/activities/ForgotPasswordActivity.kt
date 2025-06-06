package com.worldoflight.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.databinding.ActivityForgotPasswordBinding
import com.worldoflight.ui.dialogs.EmailSentDialog
import com.worldoflight.ui.viewmodels.AuthState
import com.worldoflight.ui.viewmodels.AuthViewModel

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.PasswordResetSent -> {
                    showEmailSentDialog(state.email)
                }
                else -> {
                    // Обрабатываем другие состояния
                }
            }
        }

        authViewModel.isLoading.observe(this) { isLoading ->
            binding.btnSend.isEnabled = !isLoading
            binding.btnSend.text = if (isLoading) "Отправка..." else "Отправить"
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

            btnSend.setOnClickListener {
                val email = etEmail.text.toString().trim()

                if (validateEmail(email)) {
                    authViewModel.resetPassword(email)
                }
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Введите email"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Неверный формат email"
            return false
        }

        return true
    }

    private fun showEmailSentDialog(email: String) {
        val dialog = EmailSentDialog(this, email) {
            finish()
        }
        dialog.show()
    }
}
