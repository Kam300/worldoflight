package com.worldoflight.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.worldoflight.R
import com.worldoflight.databinding.ActivityMainBinding
import com.worldoflight.ui.fragments.ProductListFragment
import com.worldoflight.ui.fragments.FavoritesFragment
import com.worldoflight.ui.fragments.CartFragment
import com.worldoflight.ui.fragments.NotificationsFragment
import com.worldoflight.ui.fragments.ProfileFragment
import com.worldoflight.ui.viewmodels.CartViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cartViewModel: CartViewModel
    private var currentSelectedNav = R.id.nav_home_container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]

        setupToolbar()
        setupBottomNavigation()
        observeCartChanges()

        if (savedInstanceState == null) {
            replaceFragment(ProductListFragment())
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            title = "Главная"
        }

        binding.toolbar.setNavigationOnClickListener {
            // TODO: Открыть боковое меню
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                selectNavItem(R.id.fab_cart)
                supportActionBar?.title = "Корзина"
                replaceFragment(CartFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupBottomNavigation() {
        // Главная
        binding.navHomeContainer.setOnClickListener {
            selectNavItem(R.id.nav_home_container)
            supportActionBar?.title = "Главная"
            replaceFragment(ProductListFragment())
        }

        // Избранное
        binding.navFavoritesContainer.setOnClickListener {
            selectNavItem(R.id.nav_favorites_container)
            supportActionBar?.title = "Избранное"
            replaceFragment(FavoritesFragment())
        }


// Корзина (центральная кнопка)
        binding.fabCart.setOnClickListener {
            val intent = android.content.Intent(this, CartActivity::class.java)
            startActivity(intent)
        }


        // Уведомления
        binding.navNotificationsContainer.setOnClickListener {
            selectNavItem(R.id.nav_notifications_container)
            supportActionBar?.title = "Уведомления"
            replaceFragment(NotificationsFragment())
        }

        // Профиль
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
        // Сброс всех иконок к неактивному состоянию
        resetAllNavItems()

        // Активация выбранной иконки
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
            R.id.fab_cart -> {
                // FAB остается с фиксированным цветом
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

    override fun onResume() {
        super.onResume()
        cartViewModel.loadCartItems(this) // Обновляем счетчик при возвращении
    }
}
