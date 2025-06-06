package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.databinding.ActivityForgotPasswordBinding
import com.worldoflight.ui.dialogs.EmailSentDialog

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            btnSend.setOnClickListener {
                val email = etEmail.text.toString().trim()

                if (validateEmail(email)) {
                    sendResetEmail(email)
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

    private fun sendResetEmail(email: String) {
        // TODO: Отправка email через Supabase
        showEmailSentDialog(email)
    }

    private fun showEmailSentDialog(email: String) {
        val dialog = EmailSentDialog(this, email) {
            finish()
        }
        dialog.show()
    }
}
