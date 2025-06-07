package com.worldoflight.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldoflight.R
import com.worldoflight.data.models.Product
import com.worldoflight.databinding.ItemPopularGridBinding
import com.worldoflight.dialogs.QuantityPickerDialog
import com.worldoflight.ui.activities.ProductDetailActivity
import com.worldoflight.utils.CartManager
import com.worldoflight.utils.FavoritesManager

class PopularGridAdapter(
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, PopularGridAdapter.PopularViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val binding = ItemPopularGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PopularViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PopularViewHolder(
        private val binding: ItemPopularGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

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

                // Установка состояния избранного
                updateFavoriteIcon(product)

                // Клик по карточке товара
                root.setOnClickListener {
                    val context = root.context
                    val intent = android.content.Intent(context, ProductDetailActivity::class.java).apply {
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_NAME, product.name)
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_PRICE, product.price)
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_CATEGORY, product.category)
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_DESCRIPTION, product.description)
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_STOCK, product.stock_quantity)
                    }
                    context.startActivity(intent)
                }

                // Клик по кнопке избранного
                ivFavorite.setOnClickListener {
                    toggleFavorite(product)
                }

                // Клик по кнопке добавления в корзину
                btnAddCart.setOnClickListener {
                    showQuantityDialog(product)
                }
            }
        }

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

        private fun updateFavoriteIcon(product: Product) {
            val isFavorite = FavoritesManager.isFavorite(binding.root.context, product.id)
            if (isFavorite) {
                binding.ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
                binding.ivFavorite.setColorFilter(
                    androidx.core.content.ContextCompat.getColor(
                        binding.root.context,
                        R.color.accent_orange
                    )
                )
            } else {
                binding.ivFavorite.setImageResource(R.drawable.ic_favorite_border)
                binding.ivFavorite.setColorFilter(
                    androidx.core.content.ContextCompat.getColor(
                        binding.root.context,
                        R.color.text_secondary
                    )
                )
            }
        }

        private fun toggleFavorite(product: Product) {
            val context = binding.root.context
            val isFavorite = FavoritesManager.isFavorite(context, product.id)

            if (isFavorite) {
                FavoritesManager.removeFromFavorites(context, product)
                Toast.makeText(
                    context,
                    "Удалено из избранного",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                FavoritesManager.addToFavorites(context, product)
                Toast.makeText(
                    context,
                    "Добавлено в избранное",
                    Toast.LENGTH_SHORT
                ).show()
            }

            updateFavoriteIcon(product)
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
