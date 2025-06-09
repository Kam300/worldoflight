package com.worldoflight.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.worldoflight.databinding.DialogOrderSuccessBinding
import com.worldoflight.ui.activities.MainActivity

class OrderSuccessDialog(
    context: Context,
    private val orderNumber: String,
    private val totalAmount: Double
) : Dialog(context) {

    private lateinit var binding: DialogOrderSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DialogOrderSuccessBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialog()
        setupClickListeners()
    }

    private fun setupDialog() {
        binding.apply {
            tvOrderNumber.text = orderNumber
            tvTotalAmount.text = "₽${String.format("%.2f", totalAmount)}"
        }

        // Настройка окна
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Диалог нельзя закрыть кнопкой назад
        setCancelable(false)
    }

    private fun setupClickListeners() {
        binding.btnContinueShopping.setOnClickListener {
            // Возвращаемся на главный экран
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            dismiss()
        }
    }
}
