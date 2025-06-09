package com.worldoflight.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.worldoflight.R
import com.worldoflight.data.models.CartItem
import com.worldoflight.databinding.ItemCartBinding
import com.worldoflight.ui.activities.ProductDetailActivity

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
                if (product != null) {
                    android.util.Log.d("PopularGridAdapter", "Image URL: ${product.image_url}")
                }

                if (!product?.image_url.isNullOrEmpty()) {
                    try {
                        Glide.with(binding.root.context)
                            .load(product?.image_url)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(getIconByCategory(product?.category ?: ""))
                            .error(getIconByCategory(product?.category ?: ""))
                            .timeout(10000)
                            .into(ivProductImage)
                    } catch (e: Exception) {
                        ivProductImage.setImageResource(getIconByCategory(product?.category ?: ""))
                    }
                } else {
                    ivProductImage.setImageResource(getIconByCategory(product?.category ?: ""))
                }
                // Отображение информации о товаре
                tvProductName.text = product?.name ?: "Неизвестный товар"
                tvProductPrice.text = "₽${String.format("%.2f", product?.price ?: cartItem.price)}"
                tvQuantity.text = cartItem.quantity.toString()

                // Расчет общей стоимости для этого товара
                val totalPrice = (product?.price ?: cartItem.price) * cartItem.quantity




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
                root.setOnClickListener {
                    val context = root.context
                    val intent = android.content.Intent(context, ProductDetailActivity::class.java).apply {
                        if (product != null) {
                            putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                        }
                        if (product != null) {
                            putExtra(ProductDetailActivity.EXTRA_PRODUCT_NAME, product.name)
                        }
                        if (product != null) {
                            putExtra(ProductDetailActivity.EXTRA_PRODUCT_PRICE, product.price)
                        }
                        if (product != null) {
                            putExtra(ProductDetailActivity.EXTRA_PRODUCT_CATEGORY, product.category)
                        }
                        if (product != null) {
                            putExtra(ProductDetailActivity.EXTRA_PRODUCT_DESCRIPTION, product.description)
                        }
                        if (product != null) {
                            putExtra(ProductDetailActivity.EXTRA_PRODUCT_STOCK, product.stock_quantity)
                        }
                        if (product != null) {
                            putExtra(ProductDetailActivity.EXTRA_PRODUCT_IMAGE_URL, product.image_url)
                        } // Добавляем URL
                    }
                    context.startActivity(intent)
                }
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
    private fun getIconByCategory(category: String): Int {
        return when (category) {
            "bulbs" -> R.drawable.ic_lightbulb
            "chandeliers" -> R.drawable.ic_chandelier
            "floor_lamps" -> R.drawable.ic_floor_lamp
            "table_lamps" -> R.drawable.ic_lightbulb
            "wall_lamps" -> R.drawable.ic_wall_lamp
            "led_strips" -> R.drawable.ic_led
            else -> R.drawable.ic_lightbulb
        }
    }
}
