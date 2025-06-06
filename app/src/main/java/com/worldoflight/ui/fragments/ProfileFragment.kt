package com.worldoflight.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.worldoflight.databinding.FragmentProfileBinding
import com.worldoflight.ui.viewmodels.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupUI()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupUI() {
        // Установка данных пользователя
        binding.apply {
            tvUserName.text = "Emmanuel Oyiboke"
            etFirstName.setText("Emmanuel")
            etLastName.setText("Oyiboke")
            etAddress.setText("Nigeria")
            etPhone.setText("+234 *** *** **67")
        }
    }

    private fun observeViewModel() {
        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.apply {
                    tvUserName.text = it.name
                    etFirstName.setText(it.name?.split(" ")?.firstOrNull() ?: "")
                    etLastName.setText(it.name?.split(" ")?.lastOrNull() ?: "")
                    etPhone.setText(it.phone)
                }
            }
        }

        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        // TODO: Добавить обработчики для сохранения изменений
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
