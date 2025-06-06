package com.worldoflight.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldoflight.R
import com.worldoflight.data.models.Notification
import com.worldoflight.databinding.ItemNotificationBinding

class NotificationsAdapter(
    private val onItemClick: (Notification) -> Unit
) : ListAdapter<Notification, NotificationsAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.apply {
                tvNotificationTitle.text = notification.title
                tvNotificationMessage.text = notification.message
                tvNotificationTime.text = getTimeAgo(notification.created_at)

                // Показать индикатор непрочитанного
                unreadIndicator.visibility = if (notification.is_read) View.GONE else View.VISIBLE

                // Установка иконки по типу уведомления
                when (notification.type) {
                    "order" -> ivNotificationIcon.setImageResource(R.drawable.ic_shopping_bag)
                    "promotion" -> ivNotificationIcon.setImageResource(R.drawable.ic_local_offer)
                    "system" -> ivNotificationIcon.setImageResource(R.drawable.ic_info)
                    else -> ivNotificationIcon.setImageResource(R.drawable.ic_notifications)
                }

                root.setOnClickListener {
                    onItemClick(notification)
                }
            }
        }

        private fun getTimeAgo(createdAt: String): String {
            // Простая реализация для демонстрации
            // В реальном приложении используйте библиотеку для работы с датами
            return "2ч"
        }
    }

    private class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}
