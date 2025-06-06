package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
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
                    performRegister(name, email, password)
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

    private fun performRegister(name: String, email: String, password: String) {
        // TODO: Здесь будет логика регистрации через Supabase
        startActivity(Intent(this, VerificationActivity::class.java).apply {
            putExtra("email", email)
        })
    }
}
