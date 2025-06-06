package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.databinding.ActivityVerificationBinding

class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email") ?: ""

        setupUI()
        setupClickListeners()
        setupOtpInput()
    }

    private fun setupUI() {
        binding.tvSubtitle.text = "Пожалуйста Проверьте Свою\nЭлектронную Почту Чтобы Увидеть Код\nПодтверждения"
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            btnVerify.setOnClickListener {
                val otp = getOtpCode()
                if (validateOtp(otp)) {
                    verifyOtp(otp)
                }
            }

            tvResend.setOnClickListener {
                resendOtp()
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

    private fun verifyOtp(otp: String) {
        // TODO: Верификация через Supabase
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun resendOtp() {
        // TODO: Повторная отправка кода
        android.widget.Toast.makeText(this, "Код отправлен повторно", android.widget.Toast.LENGTH_SHORT).show()
    }
}
