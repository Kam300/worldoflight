package com.worldoflight.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.worldoflight.R
import com.worldoflight.data.models.Product
import com.worldoflight.databinding.ActivityPopularBinding
import com.worldoflight.ui.adapters.PopularGridAdapter

class PopularActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPopularBinding
    private lateinit var popularAdapter: PopularGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopularBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadPopularProducts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Популярное"
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        popularAdapter = PopularGridAdapter { product ->
            // Обработка клика по товару
            android.widget.Toast.makeText(
                this,
                "Выбран товар: ${product.name}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }


        binding.rvPopularProducts.apply {
            layoutManager = GridLayoutManager(this@PopularActivity, 2)
            adapter = popularAdapter
        }
    }

    private fun loadPopularProducts() {
        val popularProducts = listOf(
            Product(
                id = 1,
                name = "LED лампа E27 10W",
                description = "Энергосберегающая LED лампа с теплым светом",
                price = 752.0,
                category = "bulbs",
                image_url = "",
                in_stock = true,
                brand = "Philips",
                power = "10W",
                color_temperature = "3000K"
            ),
            Product(
                id = 2,
                name = "Хрустальная люстра",
                description = "Элегантная хрустальная люстра для гостиной",
                price = 15999.0,
                category = "chandeliers",
                image_url = "",
                in_stock = true,
                brand = "Crystal Light",
                power = "60W",
                color_temperature = "2700K"
            ),
            Product(
                id = 3,
                name = "Торшер современный",
                description = "Стильный торшер в современном стиле",
                price = 8999.0,
                category = "floor_lamps",
                image_url = "",
                in_stock = true,
                brand = "Modern Light",
                power = "40W",
                color_temperature = "4000K"
            ),
            Product(
                id = 4,
                name = "Настольная лампа",
                description = "Компактная настольная лампа для работы",
                price = 2999.0,
                category = "table_lamps",
                image_url = "",
                in_stock = true,
                brand = "Desk Light",
                power = "15W",
                color_temperature = "4000K"
            ),
            Product(
                id = 5,
                name = "LED панель",
                description = "Современная LED панель для офиса",
                price = 4500.0,
                category = "led_strips",
                image_url = "",
                in_stock = true,
                brand = "Office Light",
                power = "25W",
                color_temperature = "4000K"
            ),
            Product(
                id = 6,
                name = "Бра классическое",
                description = "Классическое настенное бра",
                price = 3200.0,
                category = "wall_lamps",
                image_url = "",
                in_stock = true,
                brand = "Classic Light",
                power = "20W",
                color_temperature = "3000K"
            )
        )

        popularAdapter.submitList(popularProducts)
    }
}
