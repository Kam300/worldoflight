package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.databinding.ActivityCheckoutBinding
import com.worldoflight.ui.dialogs.OrderSuccessDialog
import com.worldoflight.ui.viewmodels.CheckoutViewModel

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val checkoutViewModel: CheckoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        observeViewModel()
        setupClickListeners()
        loadOrderSummary()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Оформление заказа"
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        checkoutViewModel.userProfile.observe(this) { profile ->
            profile?.let {
                binding.tvEmail.text = it.email ?: "email@example.com"
                binding.tvPhone.text = it.phone ?: "+7 (XXX) XXX-XX-XX"
                binding.tvAddress.text = it.address ?: "Укажите адрес доставки"
            }
        }

        checkoutViewModel.isLoading.observe(this) { isLoading ->
            binding.btnConfirmOrder.isEnabled = !isLoading
            binding.btnConfirmOrder.text = if (isLoading) "Оформляем..." else "Подтвердить"
        }

        checkoutViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                checkoutViewModel.clearError()
            }
        }

        checkoutViewModel.orderCreated.observe(this) { order ->
            order?.let {
                // Показываем диалог успешного заказа
                val dialog = OrderSuccessDialog(
                    this,
                    it.orderNumber,
                    it.totalAmount
                )
                dialog.show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnConfirmOrder.setOnClickListener {
            val email = binding.tvEmail.text.toString()
            val phone = binding.tvPhone.text.toString()
            val address = binding.tvAddress.text.toString()

            if (email.isBlank() || phone.isBlank() || address.isBlank()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkoutViewModel.createOrder(email, phone, address)
        }

        binding.paymentMethodContainer.setOnClickListener {
            // Показать диалог выбора способа оплаты
            showPaymentMethodDialog()
        }
    }

    private fun loadOrderSummary() {
        // Загружаем данные из корзины для отображения итогов
        val subtotal = intent.getDoubleExtra("subtotal", 0.0)
        val delivery = intent.getDoubleExtra("delivery", 60.20)
        val total = subtotal + delivery

        binding.tvSubtotal.text = "₽${String.format("%.2f", subtotal)}"
        binding.tvDelivery.text = "₽${String.format("%.2f", delivery)}"
        binding.tvTotal.text = "₽${String.format("%.2f", total)}"
    }

    private fun showPaymentMethodDialog() {
        val methods = arrayOf("Банковская карта", "Наличными при получении", "Электронный кошелек")

        android.app.AlertDialog.Builder(this)
            .setTitle("Выберите способ оплаты")
            .setItems(methods) { _, which ->
                val selectedMethod = when (which) {
                    0 -> "card"
                    1 -> "cash"
                    2 -> "wallet"
                    else -> "card"
                }
                checkoutViewModel.selectPaymentMethod(selectedMethod)
                Toast.makeText(this, "Выбран: ${methods[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
