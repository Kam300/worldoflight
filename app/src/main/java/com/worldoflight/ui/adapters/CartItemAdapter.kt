package com.worldoflight.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldoflight.R
import com.worldoflight.data.models.CartItem
import com.worldoflight.databinding.ItemCartSwipeBinding

class CartItemAdapter(
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : ListAdapter<CartItem, CartItemAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartSwipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class CartViewHolder(
        private val binding: ItemCartSwipeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            // Убеждаемся что элемент видим
            binding.root.visibility = android.view.View.VISIBLE

            binding.apply {
                // Отображение информации о товаре
                tvProductName.text = cartItem.product?.name ?: "Товар"
                tvProductPrice.text = "₽${String.format("%.2f", cartItem.price)}"
                tvQuantity.text = cartItem.quantity.toString()

                // Общая стоимость за все количество
                val totalPrice = cartItem.price?.times(cartItem.quantity)
                tvTotalPrice.text = "Итого: ₽${String.format("%.2f", totalPrice)}"

                // Установка изображения товара
                cartItem.product?.let { product ->
                    when (product.category) {
                        "bulbs" -> ivProductImage.setImageResource(R.drawable.ic_lightbulb)
                        "chandeliers" -> ivProductImage.setImageResource(R.drawable.ic_chandelier)
                        "floor_lamps" -> ivProductImage.setImageResource(R.drawable.ic_floor_lamp)
                        "table_lamps" -> ivProductImage.setImageResource(R.drawable.ic_lightbulb)
                        "wall_lamps" -> ivProductImage.setImageResource(R.drawable.ic_wall_lamp)
                        "led_strips" -> ivProductImage.setImageResource(R.drawable.ic_led)
                        else -> ivProductImage.setImageResource(R.drawable.ic_lightbulb)
                    }
                }

                // Обработчики кнопок количества
                btnIncrease.setOnClickListener {
                    val newQuantity = cartItem.quantity + 1
                    onQuantityChanged(cartItem, newQuantity)
                }

                btnDecrease.setOnClickListener {
                    val newQuantity = cartItem.quantity - 1
                    if (newQuantity > 0) {
                        onQuantityChanged(cartItem, newQuantity)
                    } else {
                        // Если количество становится 0, удаляем товар
                        onRemoveItem(cartItem)
                    }
                }
            }
        }
    }

    private class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem && oldItem.quantity == newItem.quantity
        }
    }
}
