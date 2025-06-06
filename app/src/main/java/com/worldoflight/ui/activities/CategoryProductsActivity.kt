package com.worldoflight.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.worldoflight.R
import com.worldoflight.data.models.Product
import com.worldoflight.databinding.ActivityCategoryProductsBinding
import com.worldoflight.ui.adapters.CategoryProductsAdapter
import com.worldoflight.ui.activities.ProductDetailActivity

class CategoryProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryProductsBinding
    private lateinit var categoryProductsAdapter: CategoryProductsAdapter

    companion object {
        const val EXTRA_CATEGORY_NAME = "category_name"
        const val EXTRA_CATEGORY_KEY = "category_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "Категория"
        val categoryKey = intent.getStringExtra(EXTRA_CATEGORY_KEY) ?: "all"

        setupToolbar(categoryName)
        setupRecyclerView()
        loadCategoryProducts(categoryKey)
    }

    private fun setupToolbar(categoryName: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = categoryName
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        categoryProductsAdapter = CategoryProductsAdapter { product ->
            openProductDetail(product)
        }

        binding.rvCategoryProducts.apply {
            layoutManager = GridLayoutManager(this@CategoryProductsActivity, 2)
            adapter = categoryProductsAdapter
        }
    }

    private fun loadCategoryProducts(categoryKey: String) {
        val allProducts = getAllProducts()

        val filteredProducts = if (categoryKey == "all") {
            allProducts
        } else {
            allProducts.filter { it.category == categoryKey }
        }

        if (filteredProducts.isEmpty()) {
            binding.rvCategoryProducts.visibility = android.view.View.GONE
            binding.emptyStateLayout.visibility = android.view.View.VISIBLE
        } else {
            binding.rvCategoryProducts.visibility = android.view.View.VISIBLE
            binding.emptyStateLayout.visibility = android.view.View.GONE
            categoryProductsAdapter.submitList(filteredProducts)
        }
    }

    private fun getAllProducts(): List<Product> {
        return listOf(
            Product(
                id = 1,
                name = "LED лампа E27 10W",
                description = "Энергосберегающая LED лампа с теплым светом",
                price = 752.0,
                category = "bulbs",
                image_url = "",
                in_stock = true,
                brand = "Philips",
                power = "10W",
                color_temperature = "3000K"
            ),
            Product(
                id = 2,
                name = "LED лампа E14 7W",
                description = "Компактная LED лампа для светильников",
                price = 599.0,
                category = "bulbs",
                image_url = "",
                in_stock = true,
                brand = "Osram",
                power = "7W",
                color_temperature = "4000K"
            ),
            Product(
                id = 3,
                name = "Хрустальная люстра",
                description = "Элегантная хрустальная люстра для гостиной",
                price = 15999.0,
                category = "chandeliers",
                image_url = "",
                in_stock = true,
                brand = "Crystal Light",
                power = "60W",
                color_temperature = "2700K"
            ),
            Product(
                id = 4,
                name = "Современная люстра",
                description = "Стильная люстра в современном стиле",
                price = 8999.0,
                category = "chandeliers",
                image_url = "",
                in_stock = true,
                brand = "Modern Light",
                power = "40W",
                color_temperature = "3000K"
            ),
            Product(
                id = 5,
                name = "Торшер классический",
                description = "Классический торшер для гостиной",
                price = 5999.0,
                category = "floor_lamps",
                image_url = "",
                in_stock = true,
                brand = "Classic Light",
                power = "60W",
                color_temperature = "2700K"
            ),
            Product(
                id = 6,
                name = "Торшер современный",
                description = "Стильный торшер в современном стиле",
                price = 8999.0,
                category = "floor_lamps",
                image_url = "",
                in_stock = true,
                brand = "Modern Light",
                power = "40W",
                color_temperature = "4000K"
            ),
            Product(
                id = 7,
                name = "Настольная лампа офисная",
                description = "Компактная настольная лампа для работы",
                price = 2999.0,
                category = "table_lamps",
                image_url = "",
                in_stock = true,
                brand = "Desk Light",
                power = "15W",
                color_temperature = "4000K"
            ),
            Product(
                id = 8,
                name = "Настольная лампа декоративная",
                description = "Декоративная настольная лампа",
                price = 3999.0,
                category = "table_lamps",
                image_url = "",
                in_stock = true,
                brand = "Deco Light",
                power = "20W",
                color_temperature = "3000K"
            ),
            Product(
                id = 9,
                name = "Бра классическое",
                description = "Классическое настенное бра",
                price = 3200.0,
                category = "wall_lamps",
                image_url = "",
                in_stock = true,
                brand = "Classic Light",
                power = "20W",
                color_temperature = "3000K"
            ),
            Product(
                id = 10,
                name = "Бра современное",
                description = "Современное настенное бра",
                price = 4200.0,
                category = "wall_lamps",
                image_url = "",
                in_stock = true,
                brand = "Modern Light",
                power = "25W",
                color_temperature = "4000K"
            ),
            Product(
                id = 11,
                name = "LED панель потолочная",
                description = "Современная LED панель для потолка",
                price = 4500.0,
                category = "led_strips",
                image_url = "",
                in_stock = true,
                brand = "Office Light",
                power = "25W",
                color_temperature = "4000K"
            ),
            Product(
                id = 12,
                name = "LED лента RGB",
                description = "Цветная LED лента с пультом управления",
                price = 1999.0,
                category = "led_strips",
                image_url = "",
                in_stock = true,
                brand = "RGB Light",
                power = "12W",
                color_temperature = "RGB"
            )
        )
    }

    private fun openProductDetail(product: Product) {
        val intent = android.content.Intent(this, ProductDetailActivity::class.java).apply {
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_NAME, product.name)
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_PRICE, product.price)
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_CATEGORY, product.category)
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_DESCRIPTION, product.description)
        }
        startActivity(intent)
    }
}
