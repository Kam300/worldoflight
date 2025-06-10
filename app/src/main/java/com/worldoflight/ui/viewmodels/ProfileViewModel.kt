package com.worldoflight.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.CheckoutRequest
import com.worldoflight.data.models.UpdateProfileRequest
import com.worldoflight.data.models.UserProfile
import com.worldoflight.data.remote.SupabaseClient
import com.worldoflight.data.repository.AuthRepository
import com.worldoflight.data.repository.OrderRepository
import com.worldoflight.utils.CartManager
import io.github.jan.supabase.gotrue.auth
// ИСПРАВЛЕНО: Используем алиас для избежания конфликта
import com.worldoflight.data.models.Order as WorldOfLightOrder
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess
    private val orderRepository = OrderRepository()


    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _orderCreated = MutableLiveData<WorldOfLightOrder?>()
    val orderCreated: LiveData<WorldOfLightOrder?> = _orderCreated

    private val _selectedPaymentMethod = MutableLiveData<String>()
    val selectedPaymentMethod: LiveData<String> = _selectedPaymentMethod
    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
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
    // НОВЫЙ МЕТОД: Обновление адреса
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
    // НОВЫЙ МЕТОД: Обновление телефона
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



    // ОБНОВЛЕНИЕ профиля с автоматической очисткой кэша
    fun updateProfile(name: String, surname: String, phone: String, address: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val updateRequest = UpdateProfileRequest(
                name = name.takeIf { it.isNotBlank() },
                surname = surname.takeIf { it.isNotBlank() },
                phone = phone.takeIf { it.isNotBlank() },
                address = address.takeIf { it.isNotBlank() }
            )

            authRepository.updateUserProfile(updateRequest)
                .onSuccess { updatedProfile ->
                    _userProfile.value = updatedProfile
                    _updateSuccess.value = true
                    android.util.Log.d("ProfileViewModel", "Profile updated and cache cleared")
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Ошибка обновления профиля"
                }

            _isLoading.value = false
        }
    }

    // ЗАГРУЗКА аватара с автоматической очисткой кэша
    fun uploadAvatar(imageBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true

            authRepository.uploadAvatar(imageBytes, fileName)
                .onSuccess { avatarUrl ->
                    // Обновляем профиль с новым URL аватара
                    val updateRequest = UpdateProfileRequest(avatar_url = avatarUrl)
                    authRepository.updateUserProfile(updateRequest)
                        .onSuccess { updatedProfile ->
                            _userProfile.value = updatedProfile
                            android.util.Log.d("ProfileViewModel", "Avatar uploaded and cache cleared")
                        }
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Ошибка загрузки аватара"
                }

            _isLoading.value = false
        }
    }

    // ПРИНУДИТЕЛЬНОЕ обновление с очисткой кэша
    fun forceRefreshProfile() {
        viewModelScope.launch {
            _isLoading.value = true

            authRepository.forceRefreshProfile()
                .onSuccess { profile ->
                    _userProfile.value = profile
                    android.util.Log.d("ProfileViewModel", "Profile force refreshed")
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Ошибка обновления профиля"
                }

            _isLoading.value = false
        }
    }

    // ИСПРАВЛЕННЫЙ метод выхода
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                    .onSuccess {
                        android.util.Log.d("ProfileViewModel", "Logged out and all caches cleared")
                        _logoutSuccess.value = true
                    }
                    .onFailure { error ->
                        android.util.Log.e("ProfileViewModel", "Logout failed", error)
                        _errorMessage.value = error.message ?: "Ошибка выхода"
                        _logoutSuccess.value = false
                    }
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Logout exception", e)
                _errorMessage.value = "Неожиданная ошибка при выходе"
                _logoutSuccess.value = false
            }
        }
    }


    fun clearError() {
        _errorMessage.value = null
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}
