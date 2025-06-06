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

    private val _subtotalPrice = MutableLiveData<Double>()
    val subtotalPrice: LiveData<Double> = _subtotalPrice

    private val _deliveryFee = MutableLiveData<Double>()
    val deliveryFee: LiveData<Double> = _deliveryFee

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
                calculateTotals(items)
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

    private fun calculateTotals(items: List<CartItem>) {
        // Подсчет суммы товаров (подытог)
        val subtotal = items.sumOf { it.price * it.quantity }
        _subtotalPrice.value = subtotal

        // Расчет доставки (бесплатная доставка от 1000 рублей)
        val delivery = if (subtotal >= 1000.0) {
            0.0
        } else {
            60.20 // Фиксированная стоимость доставки
        }
        _deliveryFee.value = delivery

        // ИТОГО = СУММА ТОВАРОВ + ДОСТАВКА
        val total = subtotal + delivery
        _totalPrice.value = total

        // Количество единиц товаров
        _itemsCount.value = items.sumOf { it.quantity }
    }
}
