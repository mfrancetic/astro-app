package com.udacity.astroapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.udacity.astroapp.data.preferences.ThemeMode
import com.udacity.astroapp.data.preferences.ThemePreferences
import org.koin.androidx.compose.get

@Composable
fun AstroThemeWrapper(
    themePreferences: ThemePreferences = get(),
    content: @Composable () -> Unit
) {
    val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme
    }
    
    AstroTheme(darkTheme = isDarkTheme, content = content)
}