package com.wajebaat.tracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.wajebaat.tracker.databinding.ActivityMainBinding
import com.wajebaat.tracker.ui.settings.ThemePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        themePreferences = ThemePreferences(this)

        // Apply theme before super.onCreate
        lifecycleScope.launch {
            val isDarkMode = themePreferences.isDarkMode.first()
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation(savedInstanceState)
    }

    private fun setupBottomNavigation(savedInstanceState: Bundle?) {
        val fragmentManager = supportFragmentManager

        // Load initial fragment
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                .replace(R.id.nav_host_container, com.wajebaat.tracker.ui.input.InputFragment())
                .commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_input -> com.wajebaat.tracker.ui.input.InputFragment()
                R.id.nav_records -> com.wajebaat.tracker.ui.records.RecordsFragment()
                R.id.nav_summary -> com.wajebaat.tracker.ui.summary.SummaryFragment()
                R.id.nav_settings -> com.wajebaat.tracker.ui.settings.SettingsFragment()
                else -> return@setOnItemSelectedListener false
            }
            fragmentManager.beginTransaction()
                .replace(R.id.nav_host_container, fragment)
                .commit()
            true
        }
    }
}
