package com.worldoflight.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldoflight.databinding.FragmentCartBinding
import com.worldoflight.ui.adapters.CartAdapter
import com.worldoflight.ui.viewmodels.CartViewModel

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
        loadCartItems()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChanged = { cartItem, newQuantity ->
                cartViewModel.updateQuantity(cartItem.id, newQuantity)
            },
            onRemoveItem = { cartItem ->
                cartViewModel.removeFromCart(cartItem.id)
            }
        )

        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun observeViewModel() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            if (cartItems.isEmpty()) {
                binding.rvCartItems.visibility = View.GONE
                binding.checkoutLayout.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
            } else {
                binding.rvCartItems.visibility = View.VISIBLE
                binding.checkoutLayout.visibility = View.VISIBLE
                binding.emptyStateLayout.visibility = View.GONE
                cartAdapter.submitList(cartItems)
            }
        }

        cartViewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
            binding.tvTotalPrice.text = "₽${String.format("%.2f", totalPrice)}"
        }

        cartViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnCheckout.setOnClickListener {
            // TODO: Переход к оформлению заказа
            android.widget.Toast.makeText(
                requireContext(),
                "Переход к оформлению заказа",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnContinueShopping.setOnClickListener {
            // Возврат к главному экрану
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun loadCartItems() {
        cartViewModel.loadCartItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
