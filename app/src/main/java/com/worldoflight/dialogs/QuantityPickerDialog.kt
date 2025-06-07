package com.worldoflight.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import com.worldoflight.databinding.DialogQuantityPickerBinding

class QuantityPickerDialog(
    context: Context,
    private val productName: String,
    private val stockQuantity: Int, // Добавляем параметр остатков
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

            // Показываем информацию об остатках
            if (stockQuantity <= 0) {
                // Товара нет в наличии
                btnAddToCart.isEnabled = false
                btnAddToCart.text = "Нет в наличии"
                btnIncrease.isEnabled = false
                btnDecrease.isEnabled = false
                tvQuantityText.text = "Товар закончился"
            } else if (stockQuantity < 5) {
                // Мало товара
                tvQuantityText.text = "Осталось только $stockQuantity шт."
            }
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
                if (selectedQuantity < stockQuantity && selectedQuantity < 99) {
                    selectedQuantity++
                    updateQuantityDisplay()
                } else if (selectedQuantity >= stockQuantity) {
                    Toast.makeText(context, "Недостаточно товара на складе", Toast.LENGTH_SHORT).show()
                }
            }

            btnCancel.setOnClickListener {
                dismiss()
            }

            btnAddToCart.setOnClickListener {
                if (stockQuantity <= 0) {
                    Toast.makeText(context, "Товар отсутствует на складе", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (selectedQuantity > stockQuantity) {
                    Toast.makeText(context, "Выбранное количество превышает остаток на складе", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                onQuantitySelected(selectedQuantity)
                dismiss()
            }
        }
    }

    private fun updateQuantityDisplay() {
        binding.tvQuantity.text = selectedQuantity.toString()
        updateTotalText()

        // Обновляем состояние кнопки добавления
        binding.btnAddToCart.isEnabled = stockQuantity > 0 && selectedQuantity <= stockQuantity
    }

    private fun updateTotalText() {
        if (stockQuantity <= 0) {
            binding.tvQuantityText.text = "Товар закончился"
            return
        }

        binding.tvQuantityText.text = when (selectedQuantity) {
            1 -> "1 товар"
            in 2..4 -> "$selectedQuantity товара"
            else -> "$selectedQuantity товаров"
        }

        // Добавляем предупреждение если остатков мало
        if (stockQuantity < 5 && selectedQuantity <= stockQuantity) {
            binding.tvQuantityText.text = "${binding.tvQuantityText.text} (осталось $stockQuantity шт.)"
        }
    }
}
