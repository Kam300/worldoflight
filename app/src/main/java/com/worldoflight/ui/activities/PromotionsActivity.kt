package com.worldoflight.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldoflight.databinding.ActivityPromotionsBinding
import com.worldoflight.ui.adapters.PromotionAdapter
import com.worldoflight.ui.viewmodels.PromotionViewModel

class PromotionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPromotionsBinding
    private val promotionViewModel: PromotionViewModel by viewModels()
    private lateinit var promotionAdapter: PromotionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromotionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        promotionViewModel.loadPromotions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Акции и скидки"
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        promotionAdapter = PromotionAdapter { promotion ->
            // Обработка клика по акции
            showPromotionDetails(promotion)
        }

        binding.rvPromotions.apply {
            layoutManager = LinearLayoutManager(this@PromotionsActivity)
            adapter = promotionAdapter
        }
    }

    private fun observeViewModel() {
        promotionViewModel.promotions.observe(this) { promotions ->
            promotionAdapter.submitList(promotions)

            if (promotions.isEmpty()) {
                binding.rvPromotions.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvPromotions.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
            }
        }

        promotionViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        promotionViewModel.error.observe(this) { error ->
            error?.let {
                android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_LONG).show()
                promotionViewModel.clearError()
            }
        }
    }

    private fun showPromotionDetails(promotion: com.worldoflight.data.models.Promotion) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(promotion.title)
            .setMessage("""
                ${promotion.description}
                
                Промокод: ${promotion.promoCode}
                Скидка: ${promotion.getDiscountText()}
                ${if (promotion.minOrderAmount > 0) "Минимальная сумма заказа: ${promotion.minOrderAmount.toInt()}₽" else ""}
                ${if (promotion.maxDiscountAmount != null) "Максимальная скидка: ${promotion.maxDiscountAmount.toInt()}₽" else ""}
            """.trimIndent())
            .setPositiveButton("Скопировать код") { _, _ ->
                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                        as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Промокод", promotion.promoCode)
                clipboard.setPrimaryClip(clip)

                android.widget.Toast.makeText(
                    this,
                    "Промокод ${promotion.promoCode} скопирован",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Закрыть", null)
            .create()

        dialog.show()
    }
}
