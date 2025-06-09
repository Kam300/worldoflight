package com.worldoflight.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldoflight.databinding.ActivityOrdersBinding
import com.worldoflight.ui.adapters.OrdersAdapter
import com.worldoflight.ui.viewmodels.OrdersViewModel

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private val ordersViewModel: OrdersViewModel by viewModels()
    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        ordersViewModel.loadOrders()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Заказы"
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(
            onOrderClick = { order ->
                showOrderDetails(order)
            },
            getOrderItems = { orderId ->
                ordersViewModel.getOrderItems(orderId)
            }
        )

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@OrdersActivity)
            adapter = ordersAdapter
        }

        // ИСПРАВЛЕНО: Правильная ссылка на SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener {
            ordersViewModel.refreshOrders()
        }
    }

    private fun observeViewModel() {
        ordersViewModel.orders.observe(this) { orders ->
            val groupedOrders = orders.groupBy { order ->
                getDateGroup(order.createdAt)
            }

            ordersAdapter.submitGroupedOrders(groupedOrders)

            if (orders.isEmpty()) {
                binding.rvOrders.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvOrders.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
            }
        }

        ordersViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // ИСПРАВЛЕНО: Правильная ссылка на SwipeRefreshLayout
            binding.swipeRefresh.isRefreshing = isLoading
        }

        ordersViewModel.error.observe(this) { error ->
            error?.let {
                android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_LONG).show()
                ordersViewModel.clearError()
            }
        }
    }

    private fun getDateGroup(dateString: String): String {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        val yesterday = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))

        return when {
            dateString.startsWith(today) -> "Сегодня"
            dateString.startsWith(yesterday) -> "Вчера"
            else -> "Ранее"
        }
    }

    private fun showOrderDetails(order: com.worldoflight.data.models.Order) {
        val orderItems = ordersViewModel.getOrderItems(order.id)
        val itemsText = orderItems.joinToString("\n") { "${it.productName} x${it.quantity}" }

        android.app.AlertDialog.Builder(this)
            .setTitle("Заказ №${order.orderNumber}")
            .setMessage("""
                Статус: ${getStatusText(order.status)}
                Сумма: ₽${String.format("%.2f", order.totalAmount)}
                Адрес: ${order.deliveryAddress}
                Способ оплаты: ${getPaymentMethodText(order.paymentMethod)}
                ${if (order.promoCode != null) "Промокод: ${order.promoCode}" else ""}
                
                Товары:
                $itemsText
            """.trimIndent())
            .setPositiveButton("Закрыть", null)
            .show()
    }

    private fun getStatusText(status: String): String {
        return when (status) {
            "pending" -> "Ожидает обработки"
            "confirmed" -> "Подтвержден"
            "shipped" -> "Отправлен"
            "delivered" -> "Доставлен"
            "cancelled" -> "Отменен"
            else -> status
        }
    }

    private fun getPaymentMethodText(method: String): String {
        return when (method) {
            "card" -> "Банковская карта"
            "cash" -> "Наличными при получении"
            "wallet" -> "Электронный кошелек"
            else -> method
        }
    }
}
