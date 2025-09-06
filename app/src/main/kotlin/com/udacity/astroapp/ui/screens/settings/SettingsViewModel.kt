package com.udacity.astroapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.data.preferences.ThemeMode
import com.udacity.astroapp.data.preferences.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class SettingsViewModel(
    private val themePreferences: ThemePreferences
) : ViewModel(), ContainerHost<SettingsState, SettingsSideEffect> {

    override val container = container<SettingsState, SettingsSideEffect>(SettingsState())
    
    init {
        loadCurrentTheme()
    }

    private fun loadCurrentTheme() = intent {
        viewModelScope.launch {
            themePreferences.themeMode.collect { themeMode ->
                reduce { state.copy(selectedTheme = themeMode) }
            }
        }
    }

    fun onThemeSelected(themeMode: ThemeMode) = intent {
        reduce { state.copy(selectedTheme = themeMode) }
        viewModelScope.launch {
            themePreferences.setThemeMode(themeMode)
        }
    }
}

data class SettingsState(
    val selectedTheme: ThemeMode = ThemeMode.SYSTEM
)

sealed class SettingsSideEffect {
    // Add side effects if needed in the future
}