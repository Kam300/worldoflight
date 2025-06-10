package com.worldoflight.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.worldoflight.R
import com.worldoflight.data.models.Product
import com.worldoflight.databinding.ItemSearchProductBinding
import com.worldoflight.ui.activities.ProductDetailActivity
import com.worldoflight.utils.CartManager

class SearchProductAdapter(
    private val onAddToCart: (Product) -> Unit
) : RecyclerView.Adapter<SearchProductAdapter.SearchProductViewHolder>() {

    private var products = listOf<Product>()

    fun submitList(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchProductViewHolder {
        val binding = ItemSearchProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class SearchProductViewHolder(
        private val binding: ItemSearchProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                // Название товара
                tvProductName.text = product.name

                // Цена
                tvProductPrice.text = "₽${String.format("%.2f", product.price)}"

                // Остатки
                tvStockQuantity.text = "В наличии: ${product.stock_quantity}"

                // Загрузка изображения
                Glide.with(ivProductImage.context)
                    .load(product.image_url)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .centerCrop()
                    .into(ivProductImage)

                // Клик по карточке - открыть детали
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

                // Кнопка добавить в корзину
                btnAddToCart.setOnClickListener {
                    if (product.stock_quantity > 0 && product.in_stock) {
                        val success = CartManager.addToCart(root.context, product, 1)
                        if (success) {
                            onAddToCart(product)
                            // Анимация кнопки
                            btnAddToCart.animate()
                                .scaleX(0.8f)
                                .scaleY(0.8f)
                                .setDuration(100)
                                .withEndAction {
                                    btnAddToCart.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .start()
                                }
                                .start()
                        }
                    }
                }

                // Состояние кнопки в зависимости от остатков
                btnAddToCart.isEnabled = product.stock_quantity > 0 && product.in_stock
                btnAddToCart.alpha = if (product.stock_quantity > 0 && product.in_stock) 1.0f else 0.5f
            }
        }
    }
}
