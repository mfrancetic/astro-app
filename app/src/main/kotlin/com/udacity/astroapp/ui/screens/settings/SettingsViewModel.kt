package com.udacity.astroapp.ui.screens.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import com.udacity.astroapp.utils.ThemePreferenceManager
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class SettingsViewModel(
    private val context: Context,
    private val themePreferenceManager: ThemePreferenceManager
) : ViewModel(), ContainerHost<SettingsState, SettingsSideEffect> {

    override val container: Container<SettingsState, SettingsSideEffect> =
        container(SettingsState())

    init {
        loadInitialState()
    }

    private fun loadInitialState() = intent {
        val appVersion = getAppVersion()
        val currentTheme = getCurrentThemeOption()

        reduce { state.copy(appVersion = appVersion, selectedTheme = currentTheme) }
    }

    fun selectTheme(themeOption: ThemeOption) = intent {
        val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val themeId =
            when (themeOption) {
                ThemeOption.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeOption.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                ThemeOption.SYSTEM ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    } else {
                        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                    }
            }

        sharedPreferences.edit().apply {
            putInt("theme", themeId)
            putInt("checkedTheme", themeOption.value)
            apply()
        }

        AppCompatDelegate.setDefaultNightMode(themeId)

        // Update the reactive theme preference manager
        themePreferenceManager.updateThemePreference(themeOption.value)

        reduce { state.copy(selectedTheme = themeOption) }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    private fun getCurrentThemeOption(): ThemeOption {
        val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val checkedTheme = sharedPreferences.getInt("checkedTheme", 2)
        return when (checkedTheme) {
            0 -> ThemeOption.LIGHT
            1 -> ThemeOption.DARK
            else -> ThemeOption.SYSTEM
        }
    }
}

sealed class SettingsSideEffect
