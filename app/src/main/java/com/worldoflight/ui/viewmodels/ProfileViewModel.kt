package com.worldoflight.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.UpdateProfileRequest
import com.worldoflight.data.models.UserProfile
import com.worldoflight.data.repository.AuthRepository
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

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true

            authRepository.getUserProfile()
                .onSuccess { profile ->
                    _userProfile.value = profile
                    // Принудительно уведомляем об изменении
                    _userProfile.postValue(profile)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Ошибка загрузки профиля"
                }

            _isLoading.value = false
        }
    }

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
                    // Принудительно перезагружаем профиль после обновления
                    loadUserProfile()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Ошибка обновления профиля"
                }

            _isLoading.value = false
        }
    }

    fun uploadAvatar(imageBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true

            authRepository.uploadAvatar(imageBytes, fileName)
                .onSuccess { avatarUrl ->
                    // Обновляем профиль с новым URL аватара
                    val currentProfile = _userProfile.value
                    if (currentProfile != null) {
                        val updateRequest = UpdateProfileRequest(
                            avatar_url = avatarUrl
                        )
                        authRepository.updateUserProfile(updateRequest)
                            .onSuccess { updatedProfile ->
                                _userProfile.value = updatedProfile
                                // Принудительно перезагружаем после загрузки аватара
                                loadUserProfile()
                            }
                    }
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Ошибка загрузки аватара"
                }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}
