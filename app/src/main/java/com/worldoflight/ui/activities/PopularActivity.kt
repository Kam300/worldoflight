package com.worldoflight.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.worldoflight.databinding.ActivityPopularBinding
import com.worldoflight.ui.adapters.PopularGridAdapter
import com.worldoflight.ui.viewmodels.ProductViewModel

class PopularActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPopularBinding
    private lateinit var popularAdapter: PopularGridAdapter
    private val productViewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopularBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()

        // Загружаем данные из Supabase
        productViewModel.loadPopularProducts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Популярное"
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        popularAdapter = PopularGridAdapter { product ->
            // Обработка клика по товару
            android.widget.Toast.makeText(
                this,
                "Выбран товар: ${product.name}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        binding.rvPopularProducts.apply {
            layoutManager = GridLayoutManager(this@PopularActivity, 2)
            adapter = popularAdapter
        }
    }

    private fun setupObservers() {
        productViewModel.popularProducts.observe(this) { products ->
            popularAdapter.submitList(products)
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
