package com.worldoflight.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldoflight.R
import com.worldoflight.databinding.ItemCategoryBinding
import com.worldoflight.ui.activities.CategoryProductsActivity
import com.worldoflight.ui.fragments.CategoryItem

class CategoryAdapter(
    private val onItemClick: (CategoryItem) -> Unit
) : ListAdapter<CategoryItem, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryItem) {
            binding.apply {
                tvCategoryName.text = category.name

                // Установка иконки по названию
                val iconRes = when (category.iconRes) {
                    "ic_all" -> R.drawable.ic_all
                    "ic_lightbulb" -> R.drawable.ic_lightbulb
                    "ic_chandelier" -> R.drawable.ic_chandelier
                    "ic_floor_lamp" -> R.drawable.ic_floor_lamp
                    "ic_wall_lamp" -> R.drawable.ic_wall_lamp
                    "ic_led" -> R.drawable.ic_led
                    else -> R.drawable.ic_lightbulb
                }

                ivCategoryIcon.setImageResource(iconRes)

                // УБИРАЕМ ДУБЛИРОВАНИЕ - оставляем только один обработчик
                root.setOnClickListener {
                    onItemClick(category)
                }
            }
        }
    }

        private fun getCategoryKey(categoryName: String): String {
            return when (categoryName) {
                "Все" -> "all"
                "Лампочки" -> "bulbs"
                "Люстры" -> "chandeliers"
                "Торшеры" -> "floor_lamps"
                "Бра" -> "wall_lamps"
                "LED" -> "led_strips"
                else -> "all"
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem == newItem
        }
    }
