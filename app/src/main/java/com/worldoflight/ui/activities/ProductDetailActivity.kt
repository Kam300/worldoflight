package com.worldoflight.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldoflight.R
import com.worldoflight.data.models.Product
import com.worldoflight.databinding.ActivityProductDetailBinding
import com.worldoflight.dialogs.QuantityPickerDialog
import com.worldoflight.ui.adapters.ProductVariantsAdapter
import com.worldoflight.utils.CartManager
import com.worldoflight.utils.FavoritesManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var variantsAdapter: ProductVariantsAdapter
    private var currentProduct: Product? = null

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
        const val EXTRA_PRODUCT_NAME = "product_name"
        const val EXTRA_PRODUCT_PRICE = "product_price"
        const val EXTRA_PRODUCT_CATEGORY = "product_category"
        const val EXTRA_PRODUCT_DESCRIPTION = "product_description"
        const val EXTRA_PRODUCT_STOCK = "product_stock"
        const val EXTRA_PRODUCT_IMAGE_URL = "product_image_url" // Добавляем новую константу

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        loadProductData()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Мир Света"
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }



    private fun loadProductData() {
        val productId = intent.getLongExtra(EXTRA_PRODUCT_ID, -1)
        val productName = intent.getStringExtra(EXTRA_PRODUCT_NAME) ?: ""
        val productPrice = intent.getDoubleExtra(EXTRA_PRODUCT_PRICE, 0.0)
        val productCategory = intent.getStringExtra(EXTRA_PRODUCT_CATEGORY) ?: ""
        val productDescription = intent.getStringExtra(EXTRA_PRODUCT_DESCRIPTION) ?: ""
        val stockQuantity = intent.getIntExtra(EXTRA_PRODUCT_STOCK, 0) // получаем остатки
        val imageUrl = intent.getStringExtra(EXTRA_PRODUCT_IMAGE_URL) // Получаем URL изображения
        currentProduct = Product(
            id = productId,
            name = productName,
            price = productPrice,
            category = productCategory,
            description = productDescription ,
            stock_quantity  = stockQuantity, // передаем остатки
            image_url = imageUrl // Добавляем URL изображения
        )

        updateUI()

    }


    private fun getCategoryDisplayName(category: String): String {
        return when (category) {
            "bulbs" -> "Лампочки"
            "chandeliers" -> "Люстры"
            "floor_lamps" -> "Торшеры"
            "table_lamps" -> "Настольные лампы"
            "wall_lamps" -> "Бра"
            "led_strips" -> "LED ленты"
            else -> "Освещение"
        }
    }

    private fun loadProductVariants() {
        // Создаем варианты товара (разные цвета/модели)
        val variants = listOf(
            ProductVariant(R.drawable.ic_lightbulb, true),
            ProductVariant(R.drawable.ic_chandelier, false),
            ProductVariant(R.drawable.ic_floor_lamp, false),
            ProductVariant(R.drawable.ic_wall_lamp, false),
            ProductVariant(R.drawable.ic_led, false)

        )

        variantsAdapter.submitList(variants)
    }

    private fun updateMainImage(variant: ProductVariant) {
        binding.ivMainProduct.setImageResource(variant.imageRes)
    }

    private fun setupClickListeners() {
        binding.apply {
            btnAddToCart.setOnClickListener {
                currentProduct?.let { product ->
                    showQuantityDialog(product)
                }
            }

            ivFavorite.setOnClickListener {
                currentProduct?.let { product ->
                    toggleFavorite(product)
                }
            }
        }
    }

    private fun toggleFavorite(product: Product) {
        val isFavorite = FavoritesManager.isFavorite(this, product.id)

        if (isFavorite) {
            FavoritesManager.removeFromFavorites(this, product)
            updateFavoriteIcon(false)
            android.widget.Toast.makeText(
                this,
                "Удалено из избранного",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            FavoritesManager.addToFavorites(this, product)
            updateFavoriteIcon(true)
            android.widget.Toast.makeText(
                this,
                "Добавлено в избранное",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    // Модель для вариантов товара
    data class ProductVariant(
        val imageRes: Int,
        val isSelected: Boolean = false
    )
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

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        if (isFavorite) {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
            binding.ivFavorite.setColorFilter(
                androidx.core.content.ContextCompat.getColor(this, R.color.accent_orange)
            )
        } else {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_border)
            binding.ivFavorite.setColorFilter(
                androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary)
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

    // Добавить в updateUI()
    private fun updateUI() {
        currentProduct?.let { product ->
            binding.apply {
                tvProductName.text = product.name
                tvProductCategory.text = getCategoryDisplayName(product.category)
                tvProductPrice.text = "₽${String.format("%.2f", product.price)}"
                tvProductDescription.text = product.description

                // Логирование для отладки
                android.util.Log.d("ProductDetailActivity", "Product: ${product.name}")
                android.util.Log.d("ProductDetailActivity", "Image URL: ${product.image_url}")

                // Загрузка изображения из URL или fallback на иконку
                if (!product.image_url.isNullOrEmpty()) {
                    try {
                        Glide.with(this@ProductDetailActivity)
                            .load(product.image_url)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(getIconByCategory(product.category))
                            .error(getIconByCategory(product.category))
                            .timeout(10000)
                            .into(ivMainProduct)
                        android.util.Log.d("Glide", "Loading image in detail: ${product.image_url}")
                    } catch (e: Exception) {
                        android.util.Log.e("Glide", "Exception loading image in detail: ${e.message}")
                        ivMainProduct.setImageResource(getIconByCategory(product.category))
                    }
                } else {
                    android.util.Log.d("ProductDetailActivity", "No image URL, using category icon")
                    ivMainProduct.setImageResource(getIconByCategory(product.category))
                }

                // Обновление состояния избранного
                val isFavorite = FavoritesManager.isFavorite(this@ProductDetailActivity, product.id)
                updateFavoriteIcon(isFavorite)
            }
        }
    }


}