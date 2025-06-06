package com.worldoflight.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.UserProfile
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Загрузка профиля из БД
                // val profile = userRepository.getUserProfile()
                // _userProfile.value = profile

                // Временные данные
                _userProfile.value = UserProfile(
                    id = 1,
                    name = "Emmanuel Oyiboke",
                    email = "emmanuel@example.com",
                    phone = "+7 (999) 123-45-67",
                    address = "Москва, ул. Примерная, д. 123"
                )
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Обновление профиля в БД
                // userRepository.updateUserProfile(profile)
                _userProfile.value = profile
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
