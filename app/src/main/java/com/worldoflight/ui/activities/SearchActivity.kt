package com.worldoflight.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldoflight.databinding.ActivitySearchBinding
import com.worldoflight.ui.adapters.SearchProductAdapter
import com.worldoflight.ui.viewmodels.SearchViewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val searchViewModel: SearchViewModel by viewModels()
    // ИСПРАВЛЕНО: Используем SearchProductAdapter вместо ProductAdapter
    private lateinit var searchProductAdapter: SearchProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        setupSearchHistory()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Поиск"
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        // ИСПРАВЛЕНО: Создаем SearchProductAdapter
        searchProductAdapter = SearchProductAdapter { product ->
            // Показываем сообщение о добавлении в корзину
            android.widget.Toast.makeText(
                this,
                "${product.name} добавлен в корзину",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchProductAdapter // ИСПРАВЛЕНО: Используем правильный адаптер
            setHasFixedSize(true)
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            // Устанавливаем фокус на SearchView
            requestFocus()
            isIconified = false

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        searchViewModel.searchProducts(it)
                        searchViewModel.addToSearchHistory(it)
                        hideSearchHistory()
                        clearFocus()
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) {
                        showSearchHistory()
                        hideSearchResults()
                    } else {
                        hideSearchHistory()
                        // Живой поиск с задержкой
                        searchViewModel.searchProducts(newText)
                    }
                    return true
                }
            })

            // Обработка фокуса
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus && query.isEmpty()) {
                    showSearchHistory()
                }
            }
        }
    }

    private fun setupSearchHistory() {
        // Загружаем историю поиска
        searchViewModel.loadSearchHistory()

        // Обработка кликов по истории поиска
        binding.llSearchHistory.setOnClickListener {
            // Клик по элементу истории
        }
    }

    private fun observeViewModel() {
        searchViewModel.searchResults.observe(this) { products ->
            // ИСПРАВЛЕНО: Используем правильный адаптер
            searchProductAdapter.submitList(products)

            if (products.isEmpty() && binding.searchView.query.isNotEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                if (products.isNotEmpty()) {
                    showSearchResults()
                }
            }
        }

        searchViewModel.searchHistory.observe(this) { history ->
            updateSearchHistory(history)
        }

        searchViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun updateSearchHistory(history: List<String>) {
        binding.llSearchHistory.removeAllViews()

        history.take(6).forEach { query ->
            val historyItem = layoutInflater.inflate(
                com.worldoflight.R.layout.item_search_history,
                binding.llSearchHistory,
                false
            )

            val textView = historyItem.findViewById<android.widget.TextView>(com.worldoflight.R.id.tv_search_query)
            textView.text = query

            historyItem.setOnClickListener {
                binding.searchView.setQuery(query, true)
            }

            binding.llSearchHistory.addView(historyItem)
        }
    }

    private fun showSearchHistory() {
        binding.scrollSearchHistory.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
    }

    private fun hideSearchHistory() {
        binding.scrollSearchHistory.visibility = View.GONE
    }

    private fun showSearchResults() {
        binding.rvSearchResults.visibility = View.VISIBLE
        binding.scrollSearchHistory.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
    }

    private fun hideSearchResults() {
        binding.rvSearchResults.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.GONE
        binding.scrollSearchHistory.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.tvEmptyState.visibility = View.GONE
    }
}
