package com.worldoflight.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldoflight.R
import com.worldoflight.data.models.Product
import com.worldoflight.databinding.ActivityProductDetailBinding
import com.worldoflight.ui.adapters.ProductVariantsAdapter
import com.worldoflight.utils.CartManager
import com.worldoflight.utils.FavoritesManager

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupVariantsRecyclerView()
        loadProductData()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "World of Light"
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupVariantsRecyclerView() {
        variantsAdapter = ProductVariantsAdapter(::updateMainImage)

        binding.rvProductVariants.apply {
            layoutManager = LinearLayoutManager(
                this@ProductDetailActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = variantsAdapter
        }
    }

    private fun loadProductData() {
        val productId = intent.getLongExtra(EXTRA_PRODUCT_ID, -1)
        val productName = intent.getStringExtra(EXTRA_PRODUCT_NAME) ?: ""
        val productPrice = intent.getDoubleExtra(EXTRA_PRODUCT_PRICE, 0.0)
        val productCategory = intent.getStringExtra(EXTRA_PRODUCT_CATEGORY) ?: ""
        val productDescription = intent.getStringExtra(EXTRA_PRODUCT_DESCRIPTION) ?: ""

        currentProduct = Product(
            id = productId,
            name = productName,
            price = productPrice,
            category = productCategory,
            description = productDescription
        )

        updateUI()
        loadProductVariants()
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
                    CartManager.addToCart(this@ProductDetailActivity, product)
                    android.widget.Toast.makeText(
                        this@ProductDetailActivity,
                        "Добавлено в корзину: ${product.name}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
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

    // Добавить в updateUI()
    private fun updateUI() {
        currentProduct?.let { product ->
            binding.apply {
                tvProductName.text = product.name
                tvProductCategory.text = getCategoryDisplayName(product.category)
                tvProductPrice.text = "₽${String.format("%.2f", product.price)}"
                tvProductDescription.text = product.description

                // Установка основного изображения
                when (product.category) {
                    "bulbs" -> ivMainProduct.setImageResource(R.drawable.ic_lightbulb)
                    "chandeliers" -> ivMainProduct.setImageResource(R.drawable.ic_chandelier)
                    "floor_lamps" -> ivMainProduct.setImageResource(R.drawable.ic_floor_lamp)
                    "table_lamps" -> ivMainProduct.setImageResource(R.drawable.ic_lightbulb)
                    "wall_lamps" -> ivMainProduct.setImageResource(R.drawable.ic_wall_lamp)
                    "led_strips" -> ivMainProduct.setImageResource(R.drawable.ic_led)
                    else -> ivMainProduct.setImageResource(R.drawable.ic_lightbulb)
                }

                // Обновление состояния избранного
                val isFavorite = FavoritesManager.isFavorite(this@ProductDetailActivity, product.id)
                updateFavoriteIcon(isFavorite)
            }
        }
    }


}