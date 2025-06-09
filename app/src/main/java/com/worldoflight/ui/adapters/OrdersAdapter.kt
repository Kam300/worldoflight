package com.worldoflight.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldoflight.data.models.Order
import com.worldoflight.data.models.OrderItem
import com.worldoflight.databinding.ItemOrderBinding
import com.worldoflight.databinding.ItemOrderHeaderBinding

class OrdersAdapter(
    private val onOrderClick: (Order) -> Unit,
    private val getOrderItems: (Long) -> List<OrderItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<OrderDisplayItem>()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ORDER = 1
    }

    sealed class OrderDisplayItem {
        data class Header(val title: String) : OrderDisplayItem()
        data class OrderData(val order: Order) : OrderDisplayItem()
    }

    fun submitGroupedOrders(groupedOrders: Map<String, List<Order>>) {
        items.clear()
        groupedOrders.forEach { (dateGroup, orders) ->
            items.add(OrderDisplayItem.Header(dateGroup))
            orders.forEach { order ->
                items.add(OrderDisplayItem.OrderData(order))
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is OrderDisplayItem.Header -> TYPE_HEADER
            is OrderDisplayItem.OrderData -> TYPE_ORDER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemOrderHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding)
            }
            TYPE_ORDER -> {
                val binding = ItemOrderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                OrderViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is OrderDisplayItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is OrderDisplayItem.OrderData -> (holder as OrderViewHolder).bind(item.order)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(
        private val binding: ItemOrderHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(title: String) {
            binding.tvDateGroup.text = title
        }
    }

    inner class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.apply {
                tvOrderNumber.text = "№ ${order.orderNumber}"
                tvOrderTime.text = getTimeFromDate(order.createdAt)
                tvOrderPrice.text = "₽${String.format("%.2f", order.totalAmount)}"

                // Получаем элементы заказа
                val orderItems = getOrderItems(order.id)
                val firstItem = orderItems.firstOrNull()

                if (firstItem != null) {
                    tvProductName.text = firstItem.productName
                    if (orderItems.size > 1) {
                        tvProductName.text = "${firstItem.productName} и еще ${orderItems.size - 1} товаров"
                    }
                } else {
                    tvProductName.text = "Заказ №${order.orderNumber}"
                }

                // Показываем статус заказа
                tvOrderStatus.text = getStatusText(order.status)
                tvOrderStatus.setTextColor(getStatusColor(order.status))

                root.setOnClickListener {
                    onOrderClick(order)
                }
            }
        }

        private fun getTimeFromDate(dateString: String): String {
            return try {
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val date = formatter.parse(dateString.replace("Z", ""))
                val timeFormatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                timeFormatter.format(date ?: java.util.Date())
            } catch (e: Exception) {
                "00:00"
            }
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

        private fun getStatusColor(status: String): Int {
            val context = binding.root.context
            return when (status) {
                "pending" -> androidx.core.content.ContextCompat.getColor(context, com.worldoflight.R.color.yellow)
                "confirmed" -> androidx.core.content.ContextCompat.getColor(context, com.worldoflight.R.color.light_blue)
                "shipped" -> androidx.core.content.ContextCompat.getColor(context, com.worldoflight.R.color.accent_orange)
                "delivered" -> androidx.core.content.ContextCompat.getColor(context, com.worldoflight.R.color.success_green)
                "cancelled" -> androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_red_dark)
                else -> androidx.core.content.ContextCompat.getColor(context, com.worldoflight.R.color.text_secondary)
            }
        }
    }
}
