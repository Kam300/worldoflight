package com.worldoflight.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.CartItem
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadCartItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Загрузка корзины из БД
                // val items = cartRepository.getCartItems()
                // _cartItems.value = items

                // Временные данные
                _cartItems.value = emptyList()
                calculateTotal()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateQuantity(cartItemId: Long, newQuantity: Int) {
        viewModelScope.launch {
            try {
                // TODO: Обновление количества в БД
                // cartRepository.updateQuantity(cartItemId, newQuantity)

                val currentItems = _cartItems.value?.toMutableList() ?: return@launch
                val updatedItems = currentItems.map { item ->
                    if (item.id == cartItemId) {
                        item.copy(quantity = newQuantity)
                    } else {
                        item
                    }
                }
                _cartItems.value = updatedItems
                calculateTotal()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun removeFromCart(cartItemId: Long) {
        viewModelScope.launch {
            try {
                // TODO: Удаление из корзины в БД
                // cartRepository.removeFromCart(cartItemId)

                val currentItems = _cartItems.value?.toMutableList() ?: return@launch
                currentItems.removeAll { it.id == cartItemId }
                _cartItems.value = currentItems
                calculateTotal()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun calculateTotal() {
        val items = _cartItems.value ?: emptyList()
        val total = items.sumOf { it.price * it.quantity }
        _totalPrice.value = total
    }
}
