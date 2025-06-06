package com.worldoflight.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.worldoflight.databinding.DialogQuantityPickerBinding

class QuantityPickerDialog(
    context: Context,
    private val productName: String,
    private val onQuantitySelected: (Int) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogQuantityPickerBinding
    private var selectedQuantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DialogQuantityPickerBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialog()
        setupClickListeners()
    }

    private fun setupDialog() {
        binding.apply {
            tvProductName.text = productName
            tvQuantity.text = selectedQuantity.toString()
            updateTotalText()
        }

        // Настройка окна
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun setupClickListeners() {
        binding.apply {
            btnDecrease.setOnClickListener {
                if (selectedQuantity > 1) {
                    selectedQuantity--
                    updateQuantityDisplay()
                }
            }

            btnIncrease.setOnClickListener {
                if (selectedQuantity < 99) {
                    selectedQuantity++
                    updateQuantityDisplay()
                }
            }

            btnCancel.setOnClickListener {
                dismiss()
            }

            btnAddToCart.setOnClickListener {
                onQuantitySelected(selectedQuantity)
                dismiss()
            }
        }
    }

    private fun updateQuantityDisplay() {
        binding.tvQuantity.text = selectedQuantity.toString()
        updateTotalText()
    }

    private fun updateTotalText() {
        binding.tvQuantityText.text = when (selectedQuantity) {
            1 -> "1 товар"
            in 2..4 -> "$selectedQuantity товара"
            else -> "$selectedQuantity товаров"
        }
    }
}
