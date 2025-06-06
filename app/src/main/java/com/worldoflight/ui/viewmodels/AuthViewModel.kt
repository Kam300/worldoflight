package com.worldoflight.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        checkAuthState()
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            authRepository.signUp(email, password, name)
                .onSuccess {
                    _authState.value = AuthState.AwaitingVerification(email)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Ошибка регистрации"
                }

            _isLoading.value = false
        }
    }

    fun verifyOtp(email: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            authRepository.verifyOtp(email, token)
                .onSuccess {
                    _authState.value = AuthState.Authenticated
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Неверный код"
                }

            _isLoading.value = false
        }
    }

    fun resendOtp(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            authRepository.resendOtp(email)
                .onSuccess {
                    _errorMessage.value = "Код отправлен повторно"
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Ошибка отправки кода"
                }

            _isLoading.value = false
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            authRepository.signIn(email, password)
                .onSuccess {
                    _authState.value = AuthState.Authenticated
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Ошибка входа"
                }

            _isLoading.value = false
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            authRepository.resetPassword(email)
                .onSuccess {
                    _authState.value = AuthState.PasswordResetSent(email)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Ошибка сброса пароля"
                }

            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
                .onSuccess {
                    _authState.value = AuthState.Unauthenticated
                }
        }
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            if (authRepository.isUserLoggedIn()) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    data class AwaitingVerification(val email: String) : AuthState()
    data class PasswordResetSent(val email: String) : AuthState()
}
