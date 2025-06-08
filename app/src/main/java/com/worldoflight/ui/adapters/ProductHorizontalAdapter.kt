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
import com.worldoflight.databinding.ItemProductHorizontalBinding
import com.worldoflight.dialogs.QuantityPickerDialog
import com.worldoflight.ui.activities.ProductDetailActivity
import com.worldoflight.utils.CartManager
import com.worldoflight.utils.FavoritesManager

class ProductHorizontalAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onAddToCart: (Product) -> Unit = {},
    private val onFavoriteClick: (Product) -> Unit = {}
) : ListAdapter<Product, ProductHorizontalAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductHorizontalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ProductViewHolder(
        private val binding: ItemProductHorizontalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product, position: Int) {
            binding.apply {
                tvProductName.text = product.name
                tvProductPrice.text = "₽${String.format("%.0f", product.price)}.00"

                // Показать "BEST SELLER" только для первых двух товаров
                if (position < 2) {
                    tvBestSeller.visibility = android.view.View.VISIBLE
                } else {
                    tvBestSeller.visibility = android.view.View.GONE
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

                // Установка состояния избранного
                updateFavoriteIcon(product)

                // Клик по всей карточке
                root.setOnClickListener {
                    onItemClick(product)
                }

                // Клик по кнопке добавления в корзину
                btnAddCart.setOnClickListener {
                    showQuantityDialog(product)
                }

                // Клик по кнопке избранного
                ivFavorite.setOnClickListener {
                    toggleFavorite(product)
                    onFavoriteClick(product)
                }
                root.setOnClickListener {
                    val context = root.context
                    val intent = android.content.Intent(context, ProductDetailActivity::class.java).apply {
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_NAME, product.name)
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_PRICE, product.price)
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_CATEGORY, product.category)
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_DESCRIPTION, product.description)
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_STOCK, product.stock_quantity)
                        putExtra(ProductDetailActivity.EXTRA_PRODUCT_IMAGE_URL, product.image_url) // Добавляем URL
                    }
                    context.startActivity(intent)
                }




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
