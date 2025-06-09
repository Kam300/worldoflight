package com.worldoflight.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.Product
import com.worldoflight.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepository = ProductRepository()
    private val sharedPrefs = application.getSharedPreferences("search_prefs", 0)

    private val _searchResults = MutableLiveData<List<Product>>()
    val searchResults: LiveData<List<Product>> = _searchResults

    private val _searchHistory = MutableLiveData<List<String>>()
    val searchHistory: LiveData<List<String>> = _searchHistory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var searchJob: Job? = null

    fun searchProducts(query: String) {
        if (query.trim().isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Задержка для оптимизации

            _isLoading.value = true
            try {
                productRepository.searchProducts(query.trim())
                    .onSuccess { products ->
                        _searchResults.value = products
                    }
                    .onFailure { exception ->
                        android.util.Log.e("SearchViewModel", "Search error", exception)
                        _searchResults.value = emptyList()
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSearchHistory() {
        val historySet = sharedPrefs.getStringSet("search_history", emptySet()) ?: emptySet()
        _searchHistory.value = historySet.toList().reversed() // Последние сверху
    }

    fun addToSearchHistory(query: String) {
        val currentHistory = _searchHistory.value?.toMutableList() ?: mutableListOf()

        // Удаляем если уже есть
        currentHistory.remove(query)
        // Добавляем в начало
        currentHistory.add(0, query)
        // Ограничиваем до 10 элементов
        if (currentHistory.size > 10) {
            currentHistory.removeAt(currentHistory.size - 1)
        }

        _searchHistory.value = currentHistory

        // Сохраняем в SharedPreferences
        sharedPrefs.edit()
            .putStringSet("search_history", currentHistory.toSet())
            .apply()
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
        sharedPrefs.edit().remove("search_history").apply()
    }
}
