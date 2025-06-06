package com.worldoflight.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.CartItem
import com.worldoflight.utils.CartManager
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice

    private val _itemsCount = MutableLiveData<Int>()
    val itemsCount: LiveData<Int> = _itemsCount

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadCartItems(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val items = CartManager.getCartItems(context)
                _cartItems.value = items
                calculateTotals(context)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateQuantity(context: Context, cartItemId: Long, newQuantity: Int) {
        viewModelScope.launch {
            try {
                CartManager.updateQuantity(context, cartItemId, newQuantity)
                loadCartItems(context)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun removeFromCart(context: Context, cartItemId: Long) {
        viewModelScope.launch {
            try {
                CartManager.removeFromCart(context, cartItemId)
                loadCartItems(context)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearCart(context: Context) {
        viewModelScope.launch {
            try {
                CartManager.clearCart(context)
                loadCartItems(context)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun calculateTotals(context: Context) {
        _totalPrice.value = CartManager.getTotalPrice(context)
        _itemsCount.value = CartManager.getCartItemsCount(context)
    }
}
