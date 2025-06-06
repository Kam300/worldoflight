package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.databinding.ActivityVerificationBinding
import com.worldoflight.ui.viewmodels.AuthState
import com.worldoflight.ui.viewmodels.AuthViewModel

class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email") ?: ""

        setupObservers()
        setupUI()
        setupClickListeners()
        setupOtpInput()
    }

    private fun setupObservers() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
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
            binding.btnVerify.isEnabled = !isLoading
            binding.btnVerify.text = if (isLoading) "Проверка..." else "Подтвердить"
        }

        authViewModel.errorMessage.observe(this) { error ->
            error?.let {
                android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_LONG).show()
                authViewModel.clearError()
                clearOtpFields()
            }
        }
    }

    private fun setupUI() {
        binding.tvSubtitle.text = "Код подтверждения отправлен на\n$email"
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            btnVerify.setOnClickListener {
                val otp = getOtpCode()
                if (validateOtp(otp)) {
                    authViewModel.verifyOtp(email, otp)
                }
            }

            tvResend.setOnClickListener {
                // TODO: Повторная отправка кода
                android.widget.Toast.makeText(this@VerificationActivity, "Код отправлен повторно", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupOtpInput() {
        val otpFields = listOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3,
            binding.etOtp4, binding.etOtp5
        )

        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    }

                    // Автоматическая проверка при заполнении всех полей
                    if (getOtpCode().length == 5) {
                        val otp = getOtpCode()
                        authViewModel.verifyOtp(email, otp)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && editText.text.isEmpty() && index > 0) {
                    otpFields[index - 1].requestFocus()
                    return@setOnKeyListener true
                }
                false
            }
        }
    }

    private fun getOtpCode(): String {
        return "${binding.etOtp1.text}${binding.etOtp2.text}${binding.etOtp3.text}${binding.etOtp4.text}${binding.etOtp5.text}"
    }

    private fun validateOtp(otp: String): Boolean {
        if (otp.length != 5) {
            android.widget.Toast.makeText(this, "Введите полный код", android.widget.Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun clearOtpFields() {
        binding.etOtp1.text?.clear()
        binding.etOtp2.text?.clear()
        binding.etOtp3.text?.clear()
        binding.etOtp4.text?.clear()
        binding.etOtp5.text?.clear()
        binding.etOtp1.requestFocus()
    }
}
