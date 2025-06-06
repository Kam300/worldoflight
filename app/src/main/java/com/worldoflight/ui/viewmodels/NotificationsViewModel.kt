package com.worldoflight.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldoflight.data.models.Notification
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Загрузка уведомлений из БД
                // val notifications = notificationRepository.getNotifications()
                // _notifications.value = notifications

                // Временные данные
                _notifications.value = getTestNotifications()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                // TODO: Отметить как прочитанное в БД
                // notificationRepository.markAsRead(notificationId)

                val currentNotifications = _notifications.value?.toMutableList() ?: return@launch
                val updatedNotifications = currentNotifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(is_read = true)
                    } else {
                        notification
                    }
                }
                _notifications.value = updatedNotifications
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun getTestNotifications(): List<Notification> {
        return listOf(
            Notification(
                id = 1,
                title = "Заказ оформлен",
                message = "Ваш заказ #1234 успешно оформлен и передан в обработку",
                type = "order",
                is_read = false,
                created_at = "2025-06-06T10:00:00Z"
            ),
            Notification(
                id = 2,
                title = "Скидка 15%",
                message = "Специальная скидка на все LED лампы до конца месяца",
                type = "promotion",
                is_read = false,
                created_at = "2025-06-05T15:30:00Z"
            ),
            Notification(
                id = 3,
                title = "Заказ доставлен",
                message = "Ваш заказ #1230 успешно доставлен",
                type = "order",
                is_read = true,
                created_at = "2025-06-04T18:45:00Z"
            )
        )
    }
}
