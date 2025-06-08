package com.worldoflight.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.worldoflight.R
import com.worldoflight.data.models.Promotion
import com.worldoflight.databinding.ItemPromotionBinding

class PromotionAdapter(
    private val onPromotionClick: (Promotion) -> Unit
) : ListAdapter<Promotion, PromotionAdapter.PromotionViewHolder>(PromotionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionViewHolder {
        val binding = ItemPromotionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PromotionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromotionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PromotionViewHolder(
        private val binding: ItemPromotionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(promotion: Promotion) {
            binding.apply {
                tvPromotionTitle.text = promotion.title
                tvPromotionDescription.text = promotion.description
                tvPromoCode.text = promotion.promoCode
                tvDiscountValue.text = promotion.getDiscountText()

                // Отображение минимальной суммы заказа
                if (promotion.minOrderAmount > 0) {
                    tvMinOrder.text = "От ${promotion.minOrderAmount.toInt()}₽"
                    tvMinOrder.visibility = android.view.View.VISIBLE
                } else {
                    tvMinOrder.visibility = android.view.View.GONE
                }

                // Загрузка изображения акции
                if (!promotion.imageUrl.isNullOrEmpty()) {
                    Glide.with(binding.root.context)
                        .load(promotion.imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_promotion_placeholder)
                        .error(R.drawable.ic_promotion_placeholder)
                        .into(ivPromotionImage)
                } else {
                    ivPromotionImage.setImageResource(R.drawable.ic_promotion_placeholder)
                }

                // Обработка клика
                root.setOnClickListener {
                    onPromotionClick(promotion)
                }

                // Копирование промокода по клику
                tvPromoCode.setOnClickListener {
                    val clipboard = root.context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                            as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Промокод", promotion.promoCode)
                    clipboard.setPrimaryClip(clip)

                    android.widget.Toast.makeText(
                        root.context,
                        "Промокод ${promotion.promoCode} скопирован",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private class PromotionDiffCallback : DiffUtil.ItemCallback<Promotion>() {
        override fun areItemsTheSame(oldItem: Promotion, newItem: Promotion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Promotion, newItem: Promotion): Boolean {
            return oldItem == newItem
        }
    }
}
