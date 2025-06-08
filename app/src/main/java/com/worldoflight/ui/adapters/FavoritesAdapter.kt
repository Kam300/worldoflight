package com.worldoflight.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.worldoflight.R
import com.worldoflight.data.models.Product
import com.worldoflight.databinding.ItemFavoriteBinding
import com.worldoflight.dialogs.QuantityPickerDialog

import com.worldoflight.utils.CartManager

class FavoritesAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onRemoveFromFavorites: (Product) -> Unit,
    private val onAddToCart: (Product) -> Unit = {}
) : ListAdapter<Product, FavoritesAdapter.FavoriteViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteViewHolder(
        private val binding: ItemFavoriteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private fun showQuantityDialog(product: Product) {
            val context = binding.root.context
            val dialog = QuantityPickerDialog(
                context,
                product.name,
                product.id, // добавляем ID товара
                product.stock_quantity
            ) { quantity ->
                val success = CartManager.addToCart(context, product, quantity)
                if (success) {
                    Toast.makeText(
                        context,
                        "Добавлено в корзину: ${product.name} (${quantity} шт.)",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Недостаточно товара на складе",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            dialog.show()
        }

        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.name
                tvProductPrice.text = "₽${String.format("%.0f", product.price)}.00"

                // Показать "BEST SELLER" для первых товаров
                if (adapterPosition < 4) {
                    tvBestSeller.visibility = android.view.View.VISIBLE
                } else {
                    tvBestSeller.visibility = android.view.View.GONE
                }



                // Иконка избранного всегда заполненная (красная)
                ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
                ivFavorite.setColorFilter(
                    androidx.core.content.ContextCompat.getColor(
                        root.context,
                        R.color.accent_orange
                    )
                )

                root.setOnClickListener {
                    onItemClick(product)
                }

                // ТОЛЬКО ОДИН обработчик кнопки добавления в корзину с диалогом
                btnAddCart.setOnClickListener {
                    showQuantityDialog(product)
                }

                ivFavorite.setOnClickListener {
                    onRemoveFromFavorites(product)
                }

                // Логирование для отладки
                android.util.Log.d("PopularGridAdapter", "Product: ${product.name}")
                android.util.Log.d("PopularGridAdapter", "Image URL: ${product.image_url}")

                // Простая загрузка изображения без listener
                if (!product.image_url.isNullOrEmpty()) {
                    try {
                        Glide.with(binding.root.context)
                            .load(product.image_url)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(getIconByCategory(product.category))
                            .error(getIconByCategory(product.category))
                            .timeout(10000) // 10 секунд таймаут
                            .into(ivProductImage)
                        android.util.Log.d("Glide", "Loading image: ${product.image_url}")
                    } catch (e: Exception) {
                        android.util.Log.e("Glide", "Exception loading image: ${e.message}")
                        ivProductImage.setImageResource(getIconByCategory(product.category))
                    }
                } else {
                    android.util.Log.d("PopularGridAdapter", "No image URL, using category icon")
                    ivProductImage.setImageResource(getIconByCategory(product.category))
                }
            }
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
    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
