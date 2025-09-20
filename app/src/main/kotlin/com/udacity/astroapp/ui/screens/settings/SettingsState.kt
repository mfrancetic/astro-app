package com.udacity.astroapp.ui.screens.settings

data class SettingsState(
    val selectedTheme: ThemeOption = ThemeOption.SYSTEM,
    val appVersion: String = ""
)

enum class ThemeOption(val displayName: String, val value: Int) {
    LIGHT("Light", 0),
    DARK("Dark", 1),
    SYSTEM("System", 2)
}
