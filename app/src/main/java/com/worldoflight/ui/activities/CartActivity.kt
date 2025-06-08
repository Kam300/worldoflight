package com.worldoflight.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldoflight.R
import com.worldoflight.databinding.ActivityCartBinding
import com.worldoflight.ui.adapters.CartAdapter
import com.worldoflight.ui.utils.DeleteSwipeCallback
import com.worldoflight.ui.viewmodels.CartViewModel

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSwipeToDelete()
        observeViewModel()

        // Загружаем данные корзины
        cartViewModel.loadCartItems(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Корзина"
        }

        // Явно устанавливаем цвет иконки навигации
        binding.toolbar.navigationIcon?.setTint(
            ContextCompat.getColor(this, R.color.text_primary)
        )

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChanged = { productId, newQuantity ->
                cartViewModel.updateQuantity(this, productId, newQuantity)
            },
            onItemRemoved = { productId ->
                cartViewModel.removeFromCart(this, productId)
                Toast.makeText(this, "Товар удален из корзины", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeToDelete() {
        val deleteSwipeCallback = DeleteSwipeCallback(this) { position ->
            val cartItems = cartViewModel.cartItems.value
            if (cartItems != null && position >= 0 && position < cartItems.size) {
                val cartItem = cartItems[position]
                cartItem.product?.let { product ->
                    cartViewModel.removeFromCart(this, product.id)
                    Toast.makeText(this, "Товар удален из корзины", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(deleteSwipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvCartItems)
    }

    private fun observeViewModel() {
        cartViewModel.cartItems.observe(this) { items ->
            cartAdapter.submitList(items?.toList())

            // Обновляем количество товаров
            val itemsText = when (items?.size ?: 0) {
                0 -> "Корзина пуста"
                1 -> "1 товар"
                in 2..4 -> "${items?.size} товара"
                else -> "${items?.size} товаров"
            }
            binding.tvItemsCount.text = itemsText

            if (items.isNullOrEmpty()) {
                binding.rvCartItems.visibility = View.GONE
                binding.btnCheckout.isEnabled = false
            } else {
                binding.rvCartItems.visibility = View.VISIBLE
                binding.btnCheckout.isEnabled = true
            }
        }

        cartViewModel.subtotalPrice.observe(this) { subtotal ->
            binding.tvSubtotal.text = "₽${String.format("%.2f", subtotal)}"
        }

        cartViewModel.deliveryFee.observe(this) { delivery ->
            binding.tvDelivery.text = if (delivery > 0) {
                "₽${String.format("%.2f", delivery)}"
            } else {
                "Бесплатно"
            }
        }

        cartViewModel.totalPrice.observe(this) { total ->
            // Можно добавить отображение общей суммы если нужно
            supportActionBar?.subtitle = "Итого: ₽${String.format("%.2f", total)}"
        }

        cartViewModel.error.observe(this) { error ->
            error?.let {
                binding.warningLayout?.visibility = View.VISIBLE
                binding.tvWarning?.text = it

                binding.warningLayout?.postDelayed({
                    binding.warningLayout?.visibility = View.GONE
                }, 3000)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        cartViewModel.loadCartItems(this)
    }
}
