package com.udacity.astroapp.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferenceManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    private val _themePreference = MutableStateFlow(getCurrentThemePreference())
    val themePreference: StateFlow<Int> = _themePreference.asStateFlow()

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "checkedTheme") {
                _themePreference.value = getCurrentThemePreference()
            }
        }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun getCurrentThemePreference(): Int {
        return sharedPreferences.getInt("checkedTheme", 2) // Default to System (2)
    }

    fun updateThemePreference(themeValue: Int) {
        sharedPreferences.edit().putInt("checkedTheme", themeValue).apply()
    }

    fun cleanup() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}
