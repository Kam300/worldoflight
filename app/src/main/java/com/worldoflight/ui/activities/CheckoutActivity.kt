package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.R
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

                // Отображаем телефон или предложение указать
                if (!it.phone.isNullOrBlank()) {
                    binding.tvPhone.text = it.phone
                    binding.tvPhoneLabel.text = "Телефон"
                } else {
                    binding.tvPhone.text = "Нажмите для указания телефона"
                    binding.tvPhoneLabel.text = "Телефон не указан"
                    binding.tvPhone.setTextColor(getColor(R.color.accent_orange))
                }

                // Отображаем адрес или предложение указать
                if (!it.address.isNullOrBlank()) {
                    binding.tvAddress.text = it.address
                } else {
                    binding.tvAddress.text = "Нажмите для указания адреса доставки"
                    binding.tvAddress.setTextColor(getColor(R.color.accent_orange))
                }
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
        // Редактирование телефона
        binding.btnEditPhone.setOnClickListener {
            showEditPhoneDialog()
        }

        // Клик по полю телефона
        binding.phoneContainer.setOnClickListener {
            showEditPhoneDialog()
        }

        // Редактирование адреса
        binding.addressContainer.setOnClickListener {
            showEditAddressDialog()
        }

        // Подтверждение заказа
        binding.btnConfirmOrder.setOnClickListener {
            validateAndCreateOrder()
        }

        // Выбор способа оплаты
        binding.paymentMethodContainer.setOnClickListener {
            showPaymentMethodDialog()
        }
    }

    private fun showEditPhoneDialog() {
        val currentPhone = binding.tvPhone.text.toString()
        val phoneToEdit = if (currentPhone == "Нажмите для указания телефона") "" else currentPhone

        val editText = EditText(this).apply {
            setText(phoneToEdit)
            hint = "+7 (XXX) XXX-XX-XX"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }

        AlertDialog.Builder(this)
            .setTitle("Укажите номер телефона")
            .setView(editText)
            .setPositiveButton("Сохранить") { _, _ ->
                val newPhone = editText.text.toString().trim()
                if (newPhone.isNotEmpty()) {
                    binding.tvPhone.text = newPhone
                    binding.tvPhoneLabel.text = "Телефон"
                    binding.tvPhone.setTextColor(getColor(R.color.text_primary))

                    // Обновляем профиль
                    checkoutViewModel.updatePhone(newPhone)
                } else {
                    Toast.makeText(this, "Введите номер телефона", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showEditAddressDialog() {
        val currentAddress = binding.tvAddress.text.toString()
        val addressToEdit = if (currentAddress == "Нажмите для указания адреса доставки") "" else currentAddress

        val editText = EditText(this).apply {
            setText(addressToEdit)
            hint = "Укажите адрес доставки"
            inputType = android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            maxLines = 3
        }

        AlertDialog.Builder(this)
            .setTitle("Укажите адрес доставки")
            .setView(editText)
            .setPositiveButton("Сохранить") { _, _ ->
                val newAddress = editText.text.toString().trim()
                if (newAddress.isNotEmpty()) {
                    binding.tvAddress.text = newAddress
                    binding.tvAddress.setTextColor(getColor(R.color.text_primary))

                    // Обновляем профиль
                    checkoutViewModel.updateAddress(newAddress)
                } else {
                    Toast.makeText(this, "Введите адрес доставки", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun validateAndCreateOrder() {
        val email = binding.tvEmail.text.toString()
        val phone = binding.tvPhone.text.toString()
        val address = binding.tvAddress.text.toString()

        // Проверяем заполненность полей
        when {
            email.isBlank() || email == "email@example.com" -> {
                Toast.makeText(this, "Email не указан", Toast.LENGTH_SHORT).show()
                return
            }
            phone.isBlank() || phone == "Нажмите для указания телефона" -> {
                showMissingPhoneDialog()
                return
            }
            address.isBlank() || address == "Нажмите для указания адреса доставки" -> {
                showMissingAddressDialog()
                return
            }
        }

        checkoutViewModel.createOrder(email, phone, address)
    }

    private fun showMissingPhoneDialog() {
        AlertDialog.Builder(this)
            .setTitle("Телефон не указан")
            .setMessage("Для оформления заказа необходимо указать номер телефона")
            .setPositiveButton("Указать телефон") { _, _ ->
                showEditPhoneDialog()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showMissingAddressDialog() {
        AlertDialog.Builder(this)
            .setTitle("Адрес не указан")
            .setMessage("Для оформления заказа необходимо указать адрес доставки")
            .setPositiveButton("Указать адрес") { _, _ ->
                showEditAddressDialog()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun loadOrderSummary() {
        val subtotal = intent.getDoubleExtra("subtotal", 0.0)
        val delivery = intent.getDoubleExtra("delivery", 60.20)
        val total = subtotal + delivery

        binding.tvSubtotal.text = "₽${String.format("%.2f", subtotal)}"
        binding.tvDelivery.text = "₽${String.format("%.2f", delivery)}"
        binding.tvTotal.text = "₽${String.format("%.2f", total)}"
    }

    private fun showPaymentMethodDialog() {
        val methods = arrayOf("Банковская карта", "Наличными при получении", "Электронный кошелек")

        AlertDialog.Builder(this)
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
