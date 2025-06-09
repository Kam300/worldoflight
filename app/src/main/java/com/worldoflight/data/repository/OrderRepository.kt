package com.worldoflight.data.repository

import com.worldoflight.data.models.CheckoutRequest
import com.worldoflight.data.models.Order
import com.worldoflight.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.text.SimpleDateFormat
import java.util.*

class OrderRepository {

    private val supabase = SupabaseClient.client

    @Serializable
    data class OrderInsert(
        @SerialName("user_id")
        val userId: String,
        @SerialName("order_number")
        val orderNumber: String,
        val status: String = "pending",
        @SerialName("total_amount")
        val totalAmount: Double,
        @SerialName("discount_amount")
        val discountAmount: Double = 0.0,
        @SerialName("delivery_fee")
        val deliveryFee: Double,
        @SerialName("payment_method")
        val paymentMethod: String,
        @SerialName("delivery_address")
        val deliveryAddress: String,
        @SerialName("contact_phone")
        val contactPhone: String,
        @SerialName("contact_email")
        val contactEmail: String,
        @SerialName("promo_code")
        val promoCode: String? = null,
        @SerialName("created_at")
        val createdAt: String,
        @SerialName("estimated_delivery")
        val estimatedDelivery: String
    )

    @Serializable
    data class OrderItemInsert(
        @SerialName("order_id")
        val orderId: Long,
        @SerialName("product_id")
        val productId: Long,
        @SerialName("product_name")
        val productName: String,
        @SerialName("product_price")
        val productPrice: Double,
        val quantity: Int,
        @SerialName("total_price")
        val totalPrice: Double
    )

    suspend fun createOrder(checkoutRequest: CheckoutRequest): Result<Order> {
        return try {
            // Получаем текущего пользователя
            val currentUser = supabase.auth.currentUserOrNull()
            android.util.Log.d("OrderRepository", "Current user: ${currentUser?.id}")

            if (currentUser == null) {
                android.util.Log.e("OrderRepository", "User not authenticated")
                return Result.failure(Exception("Пользователь не авторизован"))
            }

            // Проверяем доступность товаров
            val availabilityCheck = checkProductsAvailability(checkoutRequest.cartItems)
            if (!availabilityCheck) {
                return Result.failure(Exception("Некоторые товары недоступны в нужном количестве"))
            }

            // Генерируем номер заказа
            val orderNumber = generateOrderNumber()
            android.util.Log.d("OrderRepository", "Generated order number: $orderNumber")

            // Рассчитываем суммы
            val subtotal = checkoutRequest.cartItems.sumOf {
                (it.product?.price ?: it.price) * it.quantity
            }
            val deliveryFee = if (subtotal >= 1000.0) 0.0 else 60.20
            val discountAmount = 0.0
            val totalAmount = subtotal + deliveryFee - discountAmount

            android.util.Log.d("OrderRepository", "Order totals - Subtotal: $subtotal, Delivery: $deliveryFee, Total: $totalAmount")

            // Создаем объект заказа с РЕАЛЬНЫМ UUID пользователя
            val orderInsert = OrderInsert(
                userId = currentUser.id, // ВАЖНО: используем реальный UUID
                orderNumber = orderNumber,
                totalAmount = totalAmount,
                discountAmount = discountAmount,
                deliveryFee = deliveryFee,
                paymentMethod = checkoutRequest.paymentMethod,
                deliveryAddress = checkoutRequest.deliveryAddress,
                contactPhone = checkoutRequest.contactPhone,
                contactEmail = checkoutRequest.contactEmail,
                promoCode = checkoutRequest.promoCode,
                createdAt = getCurrentDateTime(),
                estimatedDelivery = getEstimatedDelivery()
            )

            android.util.Log.d("OrderRepository", "Inserting order with user_id: ${orderInsert.userId}")

            val createdOrders = supabase.from("orders")
                .insert(orderInsert) {
                    select()
                }
                .decodeList<Order>()

            val createdOrder = createdOrders.firstOrNull()
                ?: throw Exception("Failed to create order")

            android.util.Log.d("OrderRepository", "Order created: ${createdOrder.id}, number: ${createdOrder.orderNumber}")
            // Создаем элементы заказа и обновляем остатки
            createOrderItems(createdOrder.id, checkoutRequest.cartItems)

            Result.success(createdOrder)
        } catch (e: Exception) {
            android.util.Log.e("OrderRepository", "Error creating order: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun checkProductsAvailability(cartItems: List<com.worldoflight.data.models.CartItem>): Boolean {
        return try {
            for (cartItem in cartItems) {
                val productId = cartItem.product?.id ?: cartItem.product_id
                val requestedQuantity = cartItem.quantity

                val product = supabase.from("products")
                    .select {
                        filter {
                            eq("id", productId)
                        }
                    }
                    .decodeSingle<com.worldoflight.data.models.Product>()

                if (!product.in_stock || product.stock_quantity < requestedQuantity) {
                    android.util.Log.w("OrderRepository",
                        "Product $productId: requested $requestedQuantity, available ${product.stock_quantity}")
                    return false
                }
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("OrderRepository", "Error checking product availability", e)
            false
        }
    }

    private suspend fun createOrderItems(orderId: Long, cartItems: List<com.worldoflight.data.models.CartItem>) {
        try {
            android.util.Log.d("OrderRepository", "Creating order items for order ID: $orderId")

            val orderItems = cartItems.map { cartItem ->
                val productId = cartItem.product?.id ?: cartItem.product_id
                val quantity = cartItem.quantity

                android.util.Log.d("OrderRepository", "Adding item: Product ID $productId, Quantity $quantity")

                OrderItemInsert(
                    orderId = orderId,
                    productId = productId,
                    productName = cartItem.product?.name ?: "Товар",
                    productPrice = cartItem.product?.price ?: cartItem.price,
                    quantity = quantity,
                    totalPrice = (cartItem.product?.price ?: cartItem.price) * quantity
                )
            }

            // Вставляем элементы заказа - триггер автоматически обновит остатки
            supabase.from("order_items")
                .insert(orderItems)

            android.util.Log.d("OrderRepository", "Order items inserted - trigger should update stock")

            // УДАЛИТЕ ЭТУ СТРОКУ! Она перезаписывает изменения триггера
            // updateProductStock(cartItems)

        } catch (e: Exception) {
            android.util.Log.e("OrderRepository", "Error creating order items", e)
            throw e
        }
    }






    private fun generateOrderNumber(): String {
        val timestamp = System.currentTimeMillis()
        return "WOL${timestamp.toString().takeLast(8)}"
    }

    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun getEstimatedDelivery(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 3)
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return formatter.format(calendar.time)
    }
}
