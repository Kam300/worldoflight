package com.worldoflight.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldoflight.R
import com.worldoflight.data.models.CartItem
import com.worldoflight.databinding.ItemCartBinding

class CartAdapter(
    private val onQuantityChanged: (Long, Int) -> Unit,
    private val onItemRemoved: (Long) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.apply {
                val product = cartItem.product

                // Отображение информации о товаре
                tvProductName.text = product?.name ?: "Неизвестный товар"
                tvProductPrice.text = "₽${String.format("%.2f", product?.price ?: cartItem.price)}"
                tvQuantity.text = cartItem.quantity.toString()

                // Расчет общей стоимости для этого товара
                val totalPrice = (product?.price ?: cartItem.price) * cartItem.quantity


                // Установка изображения товара
                when (product?.category) {
                    "bulbs" -> ivProductImage.setImageResource(R.drawable.ic_lightbulb)
                    "chandeliers" -> ivProductImage.setImageResource(R.drawable.ic_chandelier)
                    "floor_lamps" -> ivProductImage.setImageResource(R.drawable.ic_floor_lamp)
                    "table_lamps" -> ivProductImage.setImageResource(R.drawable.ic_lightbulb)
                    "wall_lamps" -> ivProductImage.setImageResource(R.drawable.ic_wall_lamp)
                    "led_strips" -> ivProductImage.setImageResource(R.drawable.ic_led)
                    else -> ivProductImage.setImageResource(R.drawable.ic_lightbulb)
                }

                // Обработчик кнопки уменьшения (-)
                btnDecrease.setOnClickListener {
                    val newQuantity = cartItem.quantity - 1
                    product?.let {
                        if (newQuantity > 0) {
                            onQuantityChanged(it.id, newQuantity)
                        } else {
                            // При количестве 0 удаляем товар
                            onItemRemoved(it.id)
                        }
                    }
                }

                // Обработчик кнопки увеличения (+)
                btnIncrease.setOnClickListener {
                    val newQuantity = cartItem.quantity + 1
                    val maxStock = product?.stock_quantity ?: 999

                    if (newQuantity <= maxStock) {
                        product?.let { onQuantityChanged(it.id, newQuantity) }
                    } else {
                        android.widget.Toast.makeText(
                            binding.root.context,
                            "Недостаточно товара на складе (доступно: $maxStock шт.)",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Проверяем доступность кнопок
                val maxStock = product?.stock_quantity ?: 999
                btnIncrease.isEnabled = cartItem.quantity < maxStock
                btnDecrease.isEnabled = true // Всегда доступна

                // Визуальная обратная связь для кнопок
                val context = binding.root.context
                btnIncrease.backgroundTintList = if (btnIncrease.isEnabled) {
                    androidx.core.content.ContextCompat.getColorStateList(context, R.color.light_blue)
                } else {
                    androidx.core.content.ContextCompat.getColorStateList(context, R.color.text_secondary)
                }

                btnDecrease.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(context, R.color.light_blue)
            }
        }
    }

    private class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.product?.id == newItem.product?.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.quantity == newItem.quantity && oldItem.product == newItem.product
        }
    }
}
