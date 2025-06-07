package com.worldoflight.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.Product
import com.worldoflight.data.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val productRepository = ProductRepository()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _popularProducts = MutableLiveData<List<Product>>()
    val popularProducts: LiveData<List<Product>> = _popularProducts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Загрузка всех продуктов
    fun loadAllProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            productRepository.getAllProducts()
                .onSuccess { products ->
                    _products.value = products
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            _isLoading.value = false
        }
    }

    // Загрузка популярных продуктов
    fun loadPopularProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            productRepository.getPopularProducts()
                .onSuccess { products ->
                    _popularProducts.value = products
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            _isLoading.value = false
        }
    }

    // Загрузка продуктов по категории
    fun loadProductsByCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            productRepository.getProductsByCategory(category)
                .onSuccess { products ->
                    _products.value = products
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            _isLoading.value = false
        }
    }

    // Поиск продуктов
    fun searchProducts(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            productRepository.searchProducts(query)
                .onSuccess { products ->
                    _products.value = products
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            _isLoading.value = false
        }
    }

    // Фильтрация по цене
    fun filterByPrice(minPrice: Double, maxPrice: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            productRepository.getProductsByPriceRange(minPrice, maxPrice)
                .onSuccess { products ->
                    _products.value = products
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            _isLoading.value = false
        }
    }

    // Фильтрация по бренду
    fun filterByBrand(brand: String) {
        viewModelScope.launch {
            _isLoading.value = true
            productRepository.getProductsByBrand(brand)
                .onSuccess { products ->
                    _products.value = products
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
