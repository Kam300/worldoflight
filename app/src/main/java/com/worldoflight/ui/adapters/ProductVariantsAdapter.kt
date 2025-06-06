package com.worldoflight.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldoflight.databinding.ItemProductVariantBinding
import com.worldoflight.ui.activities.ProductDetailActivity


class ProductVariantsAdapter(
    private val onVariantClick: (ProductDetailActivity.ProductVariant) -> Unit
) : ListAdapter<ProductDetailActivity.ProductVariant, ProductVariantsAdapter.VariantViewHolder>(VariantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantViewHolder {
        val binding = ItemProductVariantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VariantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VariantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VariantViewHolder(
        private val binding: ItemProductVariantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(variant: ProductDetailActivity.ProductVariant) {
            binding.apply {
                ivVariant.setImageResource(variant.imageRes)

                // Установка состояния выбранности
                if (variant.isSelected) {
                    root.strokeWidth = 4
                    root.strokeColor = android.graphics.Color.parseColor("#60A5FA")
                } else {
                    root.strokeWidth = 0
                }

                root.setOnClickListener {
                    onVariantClick(variant)
                }

            }
        }
    }

    private class VariantDiffCallback : DiffUtil.ItemCallback<ProductDetailActivity.ProductVariant>() {
        override fun areItemsTheSame(oldItem: ProductDetailActivity.ProductVariant, newItem: ProductDetailActivity.ProductVariant): Boolean {
            return oldItem.imageRes == newItem.imageRes
        }

        override fun areContentsTheSame(oldItem: ProductDetailActivity.ProductVariant, newItem: ProductDetailActivity.ProductVariant): Boolean {
            return oldItem == newItem
        }
    }
}
