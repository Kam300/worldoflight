package com.worldoflight.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.worldoflight.R
import com.worldoflight.databinding.FragmentProfileBinding
import com.worldoflight.ui.activities.EditProfileActivity
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

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                updateUI(it)
                // Принудительно обновляем UI
                binding.root.invalidate()
            }
        }

        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_LONG).show()
                profileViewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE)
        }
    }

    private fun updateUI(profile: com.worldoflight.data.models.UserProfile) {
        binding.apply {
            // Обновляем имя пользователя
            val fullName = "${profile.name ?: ""} ${profile.surname ?: ""}".trim()
            tvUserName.text = if (fullName.isNotBlank()) fullName else "Пользователь"
            tvUserEmail.text = profile.email

            // Обновляем поля профиля с проверкой на null/empty
            tvName.text = if (!profile.name.isNullOrBlank()) profile.name else "Не указано"
            tvSurname.text = if (!profile.surname.isNullOrBlank()) profile.surname else "Не указано"
            tvAddress.text = if (!profile.address.isNullOrBlank()) profile.address else "Не указано"

            // ИСПРАВЛЕНИЕ: Правильное отображение телефона
            tvPhone.text = if (!profile.phone.isNullOrBlank()) {
                profile.phone
            } else {
                "Не указано"
            }

            // ИСПРАВЛЕНИЕ: Принудительное обновление аватара
            loadProfileImage(profile.avatar_url)
        }
    }

    // Отдельный метод для загрузки изображения
    private fun loadProfileImage(avatarUrl: String?) {
        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(binding.ivUserAvatar)
        } else {
            // Устанавливаем placeholder если URL пустой
            binding.ivUserAvatar.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    // ВАЖНО: Обновляем данные при возвращении из EditProfileActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_PROFILE_REQUEST_CODE) {
            // Принудительно перезагружаем профиль
            profileViewModel.loadUserProfile()
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные каждый раз при возвращении на экран
        profileViewModel.loadUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val EDIT_PROFILE_REQUEST_CODE = 100
    }
}
