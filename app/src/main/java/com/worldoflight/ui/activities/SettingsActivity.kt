package com.worldoflight.ui.activities
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.worldoflight.R


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Простое действие для кнопки "назад" в ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Настройки"
    }

    // Обработка кнопки "назад" в ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}