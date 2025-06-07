package com.worldoflight.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.worldoflight.databinding.DialogQuantityPickerBinding
import com.worldoflight.utils.CartManager

class QuantityPickerDialog(
    context: Context,
    private val productName: String,
    private val productId: Long,
    private val stockQuantity: Int,
    private val onQuantitySelected: (Int) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogQuantityPickerBinding
    private var selectedQuantity = 1
    private var currentQuantityInCart = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DialogQuantityPickerBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        getCurrentCartQuantity()
        setupDialog()
        setupClickListeners()
    }

    private fun getCurrentCartQuantity() {
        val cartItems = CartManager.getCartItems(context)
        currentQuantityInCart = cartItems.find { it.product?.id == productId }?.quantity ?: 0
    }

    private fun setupDialog() {
        binding.apply {
            tvProductName.text = productName
            tvQuantity.text = selectedQuantity.toString()
            updateTotalText()

            // Показываем информацию об остатках и количестве в корзине
            if (stockQuantity <= 0) {
                btnAddToCart.isEnabled = false
                btnAddToCart.text = "Нет в наличии"
                btnIncrease.isEnabled = false
                btnDecrease.isEnabled = false
                tvQuantityText.text = "Товар закончился"
            } else {
                updateAvailabilityInfo()
            }
        }

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
                val totalAfterIncrease = currentQuantityInCart + selectedQuantity + 1
                if (selectedQuantity < 99 && totalAfterIncrease <= stockQuantity) {
                    selectedQuantity++
                    updateQuantityDisplay()
                } else if (totalAfterIncrease > stockQuantity) {
                    val availableToAdd = stockQuantity - currentQuantityInCart
                    Toast.makeText(
                        context,
                        "Можно добавить еще только $availableToAdd шт. (в корзине уже $currentQuantityInCart шт.)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            btnCancel.setOnClickListener {
                dismiss()
            }

            btnAddToCart.setOnClickListener {
                val totalQuantity = currentQuantityInCart + selectedQuantity

                if (stockQuantity <= 0) {
                    Toast.makeText(context, "Товар отсутствует на складе", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (totalQuantity > stockQuantity) {
                    val availableToAdd = stockQuantity - currentQuantityInCart
                    Toast.makeText(
                        context,
                        "Недостаточно товара на складе. Доступно для добавления: $availableToAdd шт.",
                        Toast.LENGTH_LONG
                    ).show()
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
        updateAvailabilityInfo()

        val totalQuantity = currentQuantityInCart + selectedQuantity
        binding.btnAddToCart.isEnabled = stockQuantity > 0 && totalQuantity <= stockQuantity
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
    }

    private fun updateAvailabilityInfo() {
        val remainingStock = stockQuantity - currentQuantityInCart

        when {
            currentQuantityInCart > 0 -> {
                binding.tvQuantityText.text = "${binding.tvQuantityText.text}\n" +
                        "В корзине: $currentQuantityInCart шт.\n" +
                        "Доступно для добавления: $remainingStock шт."
            }
            stockQuantity < 5 -> {
                binding.tvQuantityText.text = "${binding.tvQuantityText.text}\n" +
                        "Осталось на складе: $stockQuantity шт."
            }
        }
    }
}
