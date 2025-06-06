package com.worldoflight.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.worldoflight.databinding.DialogEmailSentBinding

class EmailSentDialog(
    context: Context,
    private val email: String,
    private val onDismiss: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogEmailSentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DialogEmailSentBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialog()
        setupClickListeners()
    }

    private fun setupDialog() {
        binding.tvMessage.text = "Мы Отправили Код Восстановления\nПароля На Вашу Электронную Почту."

        // Настройка окна
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        setCancelable(false)
    }

    private fun setupClickListeners() {
        binding.btnOk.setOnClickListener {
            dismiss()
            onDismiss()
        }
    }
}
