package com.worldoflight.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.worldoflight.R
import com.worldoflight.databinding.ActivityMainBinding
import com.worldoflight.ui.fragments.*
import com.worldoflight.ui.viewmodels.CartViewModel
import com.worldoflight.ui.viewmodels.ProfileViewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cartViewModel: CartViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private var currentSelectedNav = R.id.nav_home_container

    // Элементы навигационного заголовка
    private lateinit var navHeaderAvatar: ImageView
    private lateinit var navHeaderName: TextView
    private lateinit var navHeaderEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupToolbar()
        setupDrawer()
        setupBottomNavigation()
        setupNavigationHeader()
        observeCartChanges()
        observeProfileChanges()

        if (savedInstanceState == null) {
            replaceFragment(ProductListFragment())
        }
    }

    private fun setupNavigationHeader() {
        // Получаем элементы из навигационного заголовка
        val headerView = binding.navView.getHeaderView(0)
        navHeaderAvatar = headerView.findViewById(R.id.nav_header_avatar)
        navHeaderName = headerView.findViewById(R.id.nav_header_name)
        navHeaderEmail = headerView.findViewById(R.id.nav_header_email)

        // Добавляем клик на заголовок для перехода к профилю
        headerView.setOnClickListener {
            supportActionBar?.title = "Профиль"
            replaceFragment(ProfileFragment())
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun observeProfileChanges() {
        profileViewModel.userProfile.observe(this) { profile ->
            profile?.let { updateNavigationHeader(it) }
        }

        profileViewModel.errorMessage.observe(this) { error ->
            error?.let {
                android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }
        }

        // Загружаем данные профиля
        profileViewModel.loadUserProfile()
    }

    private fun updateNavigationHeader(profile: com.worldoflight.data.models.UserProfile) {
        // Обновляем имя
        val fullName = "${profile.name ?: ""} ${profile.surname ?: ""}".trim()
        navHeaderName.text = if (fullName.isNotBlank()) fullName else "Пользователь"

        // Обновляем email
        navHeaderEmail.text = profile.email ?: "email@example.com"

        // Загружаем аватар
        if (!profile.avatar_url.isNullOrEmpty()) {
            Glide.with(this)
                .load(profile.avatar_url)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(navHeaderAvatar)
        } else {
            navHeaderAvatar.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            title = "Главная"
        }
    }

    private fun setupDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                supportActionBar?.title = "Профиль"
                replaceFragment(ProfileFragment())
            }
            R.id.nav_cart -> {
                val intent = android.content.Intent(this, CartActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_favorites -> {
                supportActionBar?.title = "Избранное"
                replaceFragment(FavoritesFragment())
            }
            // В MainActivity, в методе onNavigationItemSelected
            R.id.nav_orders -> {

                val intent = Intent(this, OrdersActivity::class.java)
                startActivity(intent)
            }

            R.id.nav_notifications -> {
                supportActionBar?.title = "Уведомления"
                replaceFragment(NotificationsFragment())
            }
            R.id.nav_settings -> {
                android.widget.Toast.makeText(this, "Настройки", android.widget.Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                // Добавьте логику выхода
                android.widget.Toast.makeText(this, "Выход", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setupBottomNavigation() {
        binding.navHomeContainer.setOnClickListener {
            selectNavItem(R.id.nav_home_container)
            supportActionBar?.title = "Главная"
            replaceFragment(ProductListFragment())
        }

        binding.navFavoritesContainer.setOnClickListener {
            selectNavItem(R.id.nav_favorites_container)
            supportActionBar?.title = "Избранное"
            replaceFragment(FavoritesFragment())
        }

        binding.fabCart.setOnClickListener {
            val intent = android.content.Intent(this, CartActivity::class.java)
            startActivity(intent)
        }



        binding.navNotificationsContainer.setOnClickListener {
            selectNavItem(R.id.nav_notifications_container)
            supportActionBar?.title = "Уведомления"
            replaceFragment(NotificationsFragment())
        }

        binding.navProfileContainer.setOnClickListener {
            selectNavItem(R.id.nav_profile_container)
            supportActionBar?.title = "Профиль"
            replaceFragment(ProfileFragment())
        }
    }

    private fun observeCartChanges() {
        cartViewModel.itemsCount.observe(this) { count ->
            updateCartBadge(count ?: 0)
        }
        cartViewModel.loadCartItems(this)
    }

    private fun updateCartBadge(count: Int) {
        if (count > 0) {
            binding.cartBadge.visibility = View.VISIBLE
            binding.cartBadge.text = if (count > 99) "99+" else count.toString()
        } else {
            binding.cartBadge.visibility = View.GONE
        }
    }

    private fun selectNavItem(selectedId: Int) {
        resetAllNavItems()

        when (selectedId) {
            R.id.nav_home_container -> {
                binding.ivNavHome.setColorFilter(ContextCompat.getColor(this, R.color.nav_icon_selected))
            }
            R.id.nav_favorites_container -> {
                binding.ivNavFavorites.setColorFilter(ContextCompat.getColor(this, R.color.nav_icon_selected))
            }
            R.id.nav_notifications_container -> {
                binding.ivNavNotifications.setColorFilter(ContextCompat.getColor(this, R.color.nav_icon_selected))
            }
            R.id.nav_profile_container -> {
                binding.ivNavProfile.setColorFilter(ContextCompat.getColor(this, R.color.nav_icon_selected))
            }
        }

        currentSelectedNav = selectedId
    }

    private fun resetAllNavItems() {
        val unselectedColor = ContextCompat.getColor(this, R.color.nav_icon_unselected)
        binding.ivNavHome.setColorFilter(unselectedColor)
        binding.ivNavFavorites.setColorFilter(unselectedColor)
        binding.ivNavNotifications.setColorFilter(unselectedColor)
        binding.ivNavProfile.setColorFilter(unselectedColor)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        cartViewModel.loadCartItems(this)
        // Обновляем данные профиля при возврате на экран
        profileViewModel.loadUserProfile()
    }
}
