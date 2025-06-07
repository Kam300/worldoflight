package com.worldoflight.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
                product.stock_quantity
            ) { quantity ->
                CartManager.addToCart(context, product, quantity) // исправлено: передаем context
                Toast.makeText(
                    context, // исправлено: передаем context
                    "Добавлено в корзину: ${product.name} (${quantity} шт.)",
                    Toast.LENGTH_SHORT
                ).show()
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

                // Установка изображения товара
                when (product.category) {
                    "bulbs" -> ivProductImage.setImageResource(R.drawable.ic_lightbulb)
                    "chandeliers" -> ivProductImage.setImageResource(R.drawable.ic_chandelier)
                    "floor_lamps" -> ivProductImage.setImageResource(R.drawable.ic_floor_lamp)
                    "table_lamps" -> ivProductImage.setImageResource(R.drawable.ic_lightbulb)
                    "wall_lamps" -> ivProductImage.setImageResource(R.drawable.ic_wall_lamp)
                    "led_strips" -> ivProductImage.setImageResource(R.drawable.ic_led)
                    else -> ivProductImage.setImageResource(R.drawable.ic_lightbulb)
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
            }
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
