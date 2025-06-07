package com.worldoflight.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.worldoflight.databinding.ActivityCategoryProductsBinding
import com.worldoflight.ui.adapters.PopularGridAdapter
import com.worldoflight.ui.viewmodels.ProductViewModel

class CategoryProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryProductsBinding
    private lateinit var categoryProductsAdapter: PopularGridAdapter
    private val productViewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryName = intent.getStringExtra("category_name") ?: "Товары"
        val categoryKey = intent.getStringExtra("category_key") ?: ""

        setupToolbar(categoryName)
        setupRecyclerView()
        setupObservers()

        // Загружаем товары по категории
        if (categoryKey.isNotEmpty()) {
            productViewModel.loadProductsByCategory(categoryKey)
        }
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
        categoryProductsAdapter = PopularGridAdapter { product ->
            android.widget.Toast.makeText(this, "Товар: ${product.name}", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.rvCategoryProducts.apply {
            layoutManager = GridLayoutManager(this@CategoryProductsActivity, 2)
            adapter = categoryProductsAdapter
        }
    }

    private fun setupObservers() {
        productViewModel.products.observe(this) { products ->
            categoryProductsAdapter.submitList(products)
        }

        productViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        productViewModel.errorMessage.observe(this) { error ->
            error?.let {
                android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_LONG).show()
                productViewModel.clearError()
            }
        }
    }
}
