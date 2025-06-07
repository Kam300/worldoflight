package com.worldoflight.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.worldoflight.R
import com.worldoflight.databinding.ActivityEditProfileBinding
import com.worldoflight.ui.viewmodels.ProfileViewModel
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Редактировать профиль"
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }



    private fun setupClickListeners() {
        binding.apply {
            btnChangePhoto.setOnClickListener {
                openImagePicker()
            }

            btnSave.setOnClickListener {
                saveProfile()
            }
        }
    }

    private fun updateUI(profile: com.worldoflight.data.models.UserProfile) {
        binding.apply {
            etName.setText(profile.name ?: "")
            etSurname.setText(profile.surname ?: "")
            etAddress.setText(profile.address ?: "")
            etPhone.setText(profile.phone ?: "")

            // Загрузка аватара
            if (!profile.avatar_url.isNullOrEmpty()) {
                Glide.with(this@EditProfileActivity)
                    .load(profile.avatar_url)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(ivUserAvatar)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            binding.ivUserAvatar.setImageBitmap(bitmap)

            // Конвертируем в ByteArray для загрузки
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val imageBytes = stream.toByteArray()

            val fileName = "avatar_${System.currentTimeMillis()}.jpg"
            profileViewModel.uploadAvatar(imageBytes, fileName)

        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Ошибка выбора изображения", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val surname = binding.etSurname.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        // Валидация телефона
        if (phone.isNotEmpty() && !isValidPhone(phone)) {
            binding.etPhone.error = "Неверный формат телефона"
            return
        }

        profileViewModel.updateProfile(name, surname, phone, address)
    }

    private fun isValidPhone(phone: String): Boolean {
        // Простая валидация телефона
        val phonePattern = Regex("^[+]?[0-9]{10,15}$")
        return phonePattern.matches(phone.replace(" ", "").replace("-", ""))
    }

    private fun setupObservers() {
        profileViewModel.userProfile.observe(this) { profile ->
            profile?.let { updateUI(it) }
        }

        profileViewModel.isLoading.observe(this) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
            binding.btnSave.text = if (isLoading) "Сохранение..." else "Сохранить"
        }

        profileViewModel.updateSuccess.observe(this) { success ->
            if (success) {
                android.widget.Toast.makeText(this, "Профиль обновлен", android.widget.Toast.LENGTH_SHORT).show()
                profileViewModel.clearUpdateSuccess()

                // ВАЖНО: Устанавливаем результат для обновления родительского фрагмента
                setResult(RESULT_OK)
                finish()
            }
        }

        profileViewModel.errorMessage.observe(this) { error ->
            error?.let {
                android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_LONG).show()
                profileViewModel.clearError()
            }
        }
    }

}