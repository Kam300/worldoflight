package com.worldoflight.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.worldoflight.data.models.Product
import com.worldoflight.databinding.FragmentFavoritesBinding
import com.worldoflight.ui.adapters.FavoritesAdapter
import com.worldoflight.ui.activities.ProductDetailActivity

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var favoritesAdapter: FavoritesAdapter
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadFavoriteProducts()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter(
            onItemClick = { product ->
                openProductDetail(product)
            },
            onRemoveFromFavorites = { product ->
                removeFromFavorites(product)
            }
        )

        binding.rvFavorites.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = favoritesAdapter
        }
    }

    private fun loadFavoriteProducts() {
        val favoriteProducts = getFavoriteProducts()

        if (favoriteProducts.isEmpty()) {
            binding.rvFavorites.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        } else {
            binding.rvFavorites.visibility = View.VISIBLE
            binding.emptyStateLayout.visibility = View.GONE
            favoritesAdapter.submitList(favoriteProducts)
        }
    }

    private fun getFavoriteProducts(): List<Product> {
        val prefs = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val favoritesJson = prefs.getString("favorite_products", "[]")
        val type = object : TypeToken<List<Product>>() {}.type
        return gson.fromJson(favoritesJson, type) ?: emptyList()
    }

    private fun removeFromFavorites(product: Product) {
        val currentFavorites = getFavoriteProducts().toMutableList()
        currentFavorites.removeAll { it.id == product.id }

        val prefs = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("favorite_products", gson.toJson(currentFavorites))
            .apply()

        loadFavoriteProducts()

        android.widget.Toast.makeText(
            requireContext(),
            "Удалено из избранного",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun openProductDetail(product: Product) {
        val intent = android.content.Intent(requireContext(), ProductDetailActivity::class.java).apply {
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_NAME, product.name)
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_PRICE, product.price)
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_CATEGORY, product.category)
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_DESCRIPTION, product.description)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadFavoriteProducts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
