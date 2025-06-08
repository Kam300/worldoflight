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
import com.worldoflight.databinding.ItemCategoryProductBinding
import com.worldoflight.dialogs.QuantityPickerDialog
import com.worldoflight.utils.CartManager
import com.worldoflight.utils.FavoritesManager

class CategoryProductsAdapter(
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, CategoryProductsAdapter.CategoryProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProductViewHolder {
        val binding = ItemCategoryProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryProductViewHolder(
        private val binding: ItemCategoryProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.name
                tvProductPrice.text = "₽${String.format("%.0f", product.price)}.00"
                tvProductBrand.text = product.brand

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

                root.setOnClickListener {
                    onItemClick(product)
                }

                btnAddCart.setOnClickListener {
                    android.widget.Toast.makeText(
                        root.context,
                        "Добавлено в корзину: ${product.name}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                // В методе bind добавить:
                btnAddCart.setOnClickListener {
                    showQuantityDialog(product)
                    android.widget.Toast.makeText(
                        root.context,
                        "Добавлено в корзину: ${product.name}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }


                ivFavorite.setOnClickListener {
                    toggleFavorite(product)
                }
                btnAddCart.setOnClickListener {
                    showQuantityDialog(product)
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

        private fun toggleFavorite(product: Product) {
            val context = binding.root.context
            val isFavorite = FavoritesManager.isFavorite(context, product.id)

            if (isFavorite) {
                FavoritesManager.removeFromFavorites(context, product)
                android.widget.Toast.makeText(
                    context,
                    "Удалено из избранного",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                FavoritesManager.addToFavorites(context, product)
                android.widget.Toast.makeText(
                    context,
                    "Добавлено в избранное",
                    android.widget.Toast.LENGTH_SHORT
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
