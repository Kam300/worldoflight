package com.worldoflight.ui.fragments

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.worldoflight.R
import com.worldoflight.data.models.Product
import com.worldoflight.databinding.FragmentProductListBinding
import com.worldoflight.ui.activities.CategoryProductsActivity
import com.worldoflight.ui.activities.PopularActivity
import com.worldoflight.ui.activities.PromotionsActivity
import com.worldoflight.ui.adapters.CategoryAdapter
import com.worldoflight.ui.adapters.ProductHorizontalAdapter
import com.worldoflight.ui.viewmodels.ProductViewModel
import android.content.Intent
import com.worldoflight.ui.activities.SearchActivity

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var popularProductsAdapter: ProductHorizontalAdapter
    private val productViewModel: ProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoriesRecyclerView()
        setupPopularProductsRecyclerView()
        setupClickListeners()
        setupObservers()
        loadData()
        setupSearchButton()
    }
    private fun setupSearchButton() {
        // Если у вас есть кнопка поиска в toolbar или layout
        binding.btnSearch?.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupCategoriesRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            val intent = android.content.Intent(requireContext(), CategoryProductsActivity::class.java).apply {
                putExtra("category_name", category.name)
                putExtra("category_key", getCategoryKey(category.name))
            }
            startActivity(intent)
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
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

    private fun setupClickListeners() {
        // Переход к популярным товарам
        binding.root.findViewById<TextView>(R.id.tv_see_all_popular)?.setOnClickListener {
            try {
                val intent = Intent(requireContext(), PopularActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("ProductListFragment", "Error starting PopularActivity", e)
                android.widget.Toast.makeText(requireContext(), "Ошибка открытия страницы", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Переход к акциям (безопасная проверка)
        try {
            val promotionsButton = binding.root.findViewById<View>(R.id.btnViewAllPromotions)
            promotionsButton?.setOnClickListener {
                try {
                    val intent = Intent(requireContext(), PromotionsActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("ProductListFragment", "Error starting PromotionsActivity", e)
                    android.widget.Toast.makeText(requireContext(), "Ошибка открытия акций", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ProductListFragment", "btnViewAllPromotions not found in layout", e)
        }
    }



    private fun setupPopularProductsRecyclerView() {
        popularProductsAdapter = ProductHorizontalAdapter(
            onItemClick = { product ->
                // Обработка клика по товару
                android.widget.Toast.makeText(
                    requireContext(),
                    "Выбран товар: ${product.name}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onAddToCart = { product ->
                // Обработка добавления в корзину
                android.widget.Toast.makeText(
                    requireContext(),
                    "Добавлено в корзину: ${product.name}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onFavoriteClick = { product ->
                // Обработка добавления в избранное
                android.widget.Toast.makeText(
                    requireContext(),
                    "Добавлено в избранное: ${product.name}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.rvPopularProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = popularProductsAdapter
            // Добавляем отступы между элементами
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.right = 16
                    // Добавляем отступ слева только для первого элемента
                    if (parent.getChildAdapterPosition(view) == 0) {
                        outRect.left = 16
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        productViewModel.popularProducts.observe(viewLifecycleOwner) { products ->
            if (products.isNotEmpty()) {
                popularProductsAdapter.submitList(products)
            } else {
                // Загружаем тестовые данные если нет данных из БД

            }
        }

        productViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_LONG).show()
                productViewModel.clearError()
                // Загружаем тестовые данные при ошибке

            }
        }
    }

    private fun loadData() {
        loadCategories()
        // Загружаем популярные товары из БД
        productViewModel.loadPopularProducts()
    }

    private fun loadCategories() {
        val categories = listOf(
            CategoryItem("Все", "ic_all"),
            CategoryItem("Лампочки", "ic_lightbulb"),
            CategoryItem("Люстры", "ic_chandelier"),
            CategoryItem("Торшеры", "ic_floor_lamp"),
            CategoryItem("Бра", "ic_wall_lamp"),
            CategoryItem("LED", "ic_led")
        )

        categoryAdapter.submitList(categories)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Модель для категорий
data class CategoryItem(
    val name: String,
    val iconRes: String
)
