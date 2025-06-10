package com.worldoflight.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.CheckoutRequest
import com.worldoflight.data.models.Order
import com.worldoflight.data.models.UpdateProfileRequest
import com.worldoflight.data.models.UserProfile
import com.worldoflight.data.remote.SupabaseClient
import com.worldoflight.data.repository.AuthRepository
import com.worldoflight.data.repository.OrderRepository
import com.worldoflight.utils.CartManager
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val orderRepository = OrderRepository()

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _orderCreated = MutableLiveData<Order?>()
    val orderCreated: LiveData<Order?> = _orderCreated

    private val _selectedPaymentMethod = MutableLiveData<String>()
    val selectedPaymentMethod: LiveData<String> = _selectedPaymentMethod

    init {
        loadUserProfile()
        _selectedPaymentMethod.value = "card"
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.getUserProfile()
                .onSuccess { profile ->
                    _userProfile.value = profile
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    // МЕТОД: Обновление телефона
    fun updatePhone(phone: String) {
        viewModelScope.launch {
            val updateRequest = UpdateProfileRequest(phone = phone)
            authRepository.updateUserProfile(updateRequest)
                .onSuccess { updatedProfile ->
                    _userProfile.value = updatedProfile
                    android.util.Log.d("CheckoutViewModel", "Phone updated successfully")
                }
                .onFailure { exception ->
                    android.util.Log.e("CheckoutViewModel", "Failed to update phone", exception)
                }
        }
    }

    // МЕТОД: Обновление адреса
    fun updateAddress(address: String) {
        viewModelScope.launch {
            val updateRequest = UpdateProfileRequest(address = address)
            authRepository.updateUserProfile(updateRequest)
                .onSuccess { updatedProfile ->
                    _userProfile.value = updatedProfile
                    android.util.Log.d("CheckoutViewModel", "Address updated successfully")
                }
                .onFailure { exception ->
                    android.util.Log.e("CheckoutViewModel", "Failed to update address", exception)
                }
        }
    }

    fun createOrder(email: String, phone: String, address: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser == null) {
                    _error.value = "Необходимо войти в аккаунт для оформления заказа"
                    return@launch
                }

                android.util.Log.d("CheckoutViewModel", "Creating order for user: ${currentUser.id}")

                val cartItems = CartManager.getCartItems(getApplication())
                if (cartItems.isEmpty()) {
                    _error.value = "Корзина пуста"
                    return@launch
                }

                val checkoutRequest = CheckoutRequest(
                    contactEmail = email,
                    contactPhone = phone,
                    deliveryAddress = address,
                    paymentMethod = _selectedPaymentMethod.value ?: "card",
                    cartItems = cartItems
                )

                orderRepository.createOrder(checkoutRequest)
                    .onSuccess { order ->
                        _orderCreated.value = order
                        CartManager.clearCart(getApplication())
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Ошибка при создании заказа"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Неизвестная ошибка"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
