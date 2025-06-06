package com.worldoflight.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldoflight.databinding.ActivityCartBinding
import com.worldoflight.ui.adapters.CartItemAdapter
import com.worldoflight.ui.utils.SwipeToActionCallback
import com.worldoflight.ui.viewmodels.CartViewModel

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartAdapter: CartItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupSwipeGestures()
        observeViewModel()
        setupClickListeners()
        loadCartItems()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Корзина"
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartItemAdapter(
            onQuantityChanged = { cartItem, newQuantity ->
                cartViewModel.updateQuantity(this, cartItem.id, newQuantity)
            },
            onRemoveItem = { cartItem ->
                cartViewModel.removeFromCart(this, cartItem.id)
            }
        )

        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun setupSwipeGestures() {
        val swipeCallback = object : SwipeToActionCallback(this) {
            override fun onLeftSwipe(position: Int) {
                // Удаление товара при свайпе влево
                val cartItem = cartAdapter.currentList[position]
                cartViewModel.removeFromCart(this@CartActivity, cartItem.id)

                android.widget.Toast.makeText(
                    this@CartActivity,
                    "Товар удален из корзины",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }

            override fun onRightSwipeAction(position: Int, action: SwipeAction) {
                val cartItem = cartAdapter.currentList[position]

                when (action) {
                    SwipeAction.DECREASE -> {
                        val newQuantity = cartItem.quantity - 1
                        if (newQuantity > 0) {
                            cartViewModel.updateQuantity(this@CartActivity, cartItem.id, newQuantity)
                            android.widget.Toast.makeText(
                                this@CartActivity,
                                "Количество уменьшено",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            cartViewModel.removeFromCart(this@CartActivity, cartItem.id)
                            android.widget.Toast.makeText(
                                this@CartActivity,
                                "Товар удален",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    SwipeAction.INCREASE -> {
                        val newQuantity = cartItem.quantity + 1
                        cartViewModel.updateQuantity(this@CartActivity, cartItem.id, newQuantity)
                        android.widget.Toast.makeText(
                            this@CartActivity,
                            "Количество увеличено",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Возвращаем элемент в исходное положение
                cartAdapter.notifyItemChanged(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvCartItems)
    }

    private fun observeViewModel() {
        cartViewModel.cartItems.observe(this) { cartItems ->
            if (cartItems.isEmpty()) {
                binding.rvCartItems.visibility = android.view.View.GONE
                binding.checkoutLayout.visibility = android.view.View.GONE
                binding.emptyStateLayout.visibility = android.view.View.VISIBLE
            } else {
                binding.rvCartItems.visibility = android.view.View.VISIBLE
                binding.checkoutLayout.visibility = android.view.View.VISIBLE
                binding.emptyStateLayout.visibility = android.view.View.GONE
                cartAdapter.submitList(cartItems)

                // Обновляем количество товаров в заголовке
                val itemCount = cartItems.size
                binding.tvItemCount.text = when (itemCount) {
                    1 -> "1 товар"
                    in 2..4 -> "$itemCount товара"
                    else -> "$itemCount товаров"
                }
            }
        }

        cartViewModel.totalPrice.observe(this) { totalPrice ->
            binding.tvTotalPrice.text = "₽${String.format("%.0f", totalPrice)}"
            binding.tvSubtotal.text = "₽${String.format("%.0f", totalPrice)}"
        }

        cartViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        cartViewModel.error.observe(this) { error ->
            error?.let {
                android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnCheckout.setOnClickListener {
            android.widget.Toast.makeText(
                this,
                "Оформить заказ на сумму ${binding.tvTotalPrice.text}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadCartItems() {
        cartViewModel.loadCartItems(this)
    }
}
