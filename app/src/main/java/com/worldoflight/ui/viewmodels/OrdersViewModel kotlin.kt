package com.worldoflight.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.Order
import com.worldoflight.data.models.OrderItem
import com.worldoflight.data.repository.OrderRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class OrdersViewModel : ViewModel() {

    private val orderRepository = OrderRepository()

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _orderItems = MutableLiveData<Map<Long, List<OrderItem>>>()
    val orderItems: LiveData<Map<Long, List<OrderItem>>> = _orderItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("OrdersViewModel", "Loading user orders...")

                orderRepository.getUserOrders()
                    .onSuccess { ordersList ->
                        _orders.value = ordersList
                        android.util.Log.d("OrdersViewModel", "Loaded ${ordersList.size} orders")

                        // Загружаем элементы для каждого заказа
                        loadOrderItems(ordersList)
                    }
                    .onFailure { exception ->
                        android.util.Log.e("OrdersViewModel", "Failed to load orders", exception)
                        _error.value = exception.message

                        // Загружаем тестовые данные при ошибке
                        loadTestOrders()
                    }
            } catch (e: Exception) {
                android.util.Log.e("OrdersViewModel", "Error in loadOrders", e)
                _error.value = e.message
                loadTestOrders()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadOrderItems(orders: List<Order>) {
        val itemsMap = mutableMapOf<Long, List<OrderItem>>()

        orders.forEach { order ->
            orderRepository.getOrderItems(order.id)
                .onSuccess { items ->
                    itemsMap[order.id] = items
                }
                .onFailure { exception ->
                    android.util.Log.w("OrdersViewModel", "Failed to load items for order ${order.id}", exception)
                    itemsMap[order.id] = emptyList()
                }
        }

        _orderItems.value = itemsMap
    }

    private fun loadTestOrders() {
        android.util.Log.d("OrdersViewModel", "Loading test orders as fallback")

        val testOrders = listOf(
            Order(
                id = 1,
                userId = "test_user",
                orderNumber = "WOL65751642",
                status = "pending",
                totalAmount = 8840.0,
                discountAmount = 0.0,
                deliveryFee = 0.0,
                paymentMethod = "card",
                deliveryAddress = "Магнитогорск, ул. Ленина 1",
                contactPhone = "+7 (XXX) XXX-XX-XX",
                contactEmail = "user@example.com",
                promoCode = "LIGHT20",
                createdAt = getCurrentDateTime(),
                estimatedDelivery = getEstimatedDelivery()
            ),
            Order(
                id = 2,
                userId = "test_user",
                orderNumber = "WOL65776292",
                status = "confirmed",
                totalAmount = 5200.0,
                discountAmount = 0.0,
                deliveryFee = 0.0,
                paymentMethod = "cash",
                deliveryAddress = "Магнитогорск, ул. Ленина 1",
                contactPhone = "+7 (XXX) XXX-XX-XX",
                contactEmail = "user@example.com",
                createdAt = getCurrentDateTime(),
                estimatedDelivery = getEstimatedDelivery()
            )
        )

        _orders.value = testOrders

        // Тестовые элементы заказов
        val testItems = mapOf(
            1L to listOf(
                OrderItem(
                    id = 1,
                    orderId = 1,
                    productId = 2,
                    productName = "LED лампа E14 7W",
                    productPrice = 520.0,
                    quantity = 17,
                    totalPrice = 8840.0
                )
            ),
            2L to listOf(
                OrderItem(
                    id = 2,
                    orderId = 2,
                    productId = 2,
                    productName = "LED лампа E14 7W",
                    productPrice = 520.0,
                    quantity = 10,
                    totalPrice = 5200.0
                )
            )
        )

        _orderItems.value = testItems
    }

    fun getOrderItems(orderId: Long): List<OrderItem> {
        return _orderItems.value?.get(orderId) ?: emptyList()
    }

    fun refreshOrders() {
        loadOrders()
    }

    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }

    private fun getEstimatedDelivery(): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 3)
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    fun clearError() {
        _error.value = null
    }
}
