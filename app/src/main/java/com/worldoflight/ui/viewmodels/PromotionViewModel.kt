package com.worldoflight.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.Promotion
import com.worldoflight.data.repository.PromotionRepository
import kotlinx.coroutines.launch

class PromotionViewModel : ViewModel() {

    private val promotionRepository = PromotionRepository()

    private val _promotions = MutableLiveData<List<Promotion>>()
    val promotions: LiveData<List<Promotion>> = _promotions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _selectedPromotion = MutableLiveData<Promotion?>()
    val selectedPromotion: LiveData<Promotion?> = _selectedPromotion

    fun loadPromotions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                promotionRepository.getActivePromotions()
                    .onSuccess { promotionList ->
                        _promotions.value = promotionList
                    }
                    .onFailure { exception ->
                        _error.value = exception.message
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun validatePromoCode(promoCode: String, callback: (Promotion?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                promotionRepository.getPromotionByCode(promoCode)
                    .onSuccess { promotion ->
                        if (promotion != null && promotion.isValid()) {
                            callback(promotion, null)
                        } else {
                            callback(null, "Промокод недействителен или истек")
                        }
                    }
                    .onFailure { exception ->
                        callback(null, exception.message)
                    }
            } catch (e: Exception) {
                callback(null, e.message)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
