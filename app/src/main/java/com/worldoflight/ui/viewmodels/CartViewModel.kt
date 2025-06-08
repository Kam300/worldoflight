package com.worldoflight.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.CartItem
import com.worldoflight.data.models.Promotion
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

    private val promotionViewModel = PromotionViewModel()

    private val _appliedPromotion = MutableLiveData<Promotion?>()
    val appliedPromotion: LiveData<Promotion?> = _appliedPromotion

    private val _discountAmount = MutableLiveData<Double>()
    val discountAmount: LiveData<Double> = _discountAmount

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

    fun updateQuantity(context: Context, productId: Long, newQuantity: Int) {
        viewModelScope.launch {
            try {
                val success = CartManager.updateQuantity(context, productId, newQuantity)
                if (success) {
                    loadCartItems(context)
                } else {
                    _error.value = "Недостаточно товара на складе"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun removeFromCart(context: Context, productId: Long) {
        viewModelScope.launch {
            try {
                CartManager.removeFromCart(context, productId)
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
        val subtotal = items.sumOf { (it.product?.price ?: it.price) * it.quantity }
        _subtotalPrice.value = subtotal

        val delivery = if (subtotal >= 1000.0) 0.0 else 60.20
        _deliveryFee.value = delivery

        // Применяем скидку если есть промокод
        val discount = _discountAmount.value ?: 0.0
        val total = subtotal + delivery - discount
        _totalPrice.value = total

        _itemsCount.value = items.sumOf { it.quantity }
    }
    fun applyPromoCode(promoCode: String, subtotal: Double) {
        promotionViewModel.validatePromoCode(promoCode) { promotion, error ->
            if (promotion != null) {
                val discount = calculateDiscount(promotion, subtotal)
                if (discount > 0) {
                    _appliedPromotion.value = promotion
                    _discountAmount.value = discount
                    calculateTotals(cartItems.value ?: emptyList())
                } else {
                    _error.value = "Промокод не применим к данному заказу"
                }
            } else {
                _error.value = error ?: "Неверный промокод"
            }
        }
    }
    private fun calculateDiscount(promotion: Promotion, subtotal: Double): Double {
        if (subtotal < promotion.minOrderAmount) return 0.0

        return when (promotion.discountType) {
            "percentage" -> {
                val discount = subtotal * promotion.discountValue / 100
                promotion.maxDiscountAmount?.let { maxDiscount ->
                    minOf(discount, maxDiscount)
                } ?: discount
            }
            "fixed_amount" -> promotion.discountValue
            else -> 0.0
        }
    }
    fun removePromoCode() {
        _appliedPromotion.value = null
        _discountAmount.value = 0.0
        calculateTotals(cartItems.value ?: emptyList())
    }

}
