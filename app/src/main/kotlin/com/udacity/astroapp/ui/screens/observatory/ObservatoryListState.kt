package com.udacity.astroapp.ui.screens.observatory

import com.udacity.astroapp.models.Observatory

data class ObservatoryListState(
    val observatories: List<Observatory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val currentLocation: Pair<Double, Double>? = null,
    val showLocationPermissionDialog: Boolean = false,
    val showLocationSettingsDialog: Boolean = false
)

sealed class ObservatoryListSideEffect {
    data class ShowError(val message: String) : ObservatoryListSideEffect()
    data class NavigateToObservatoryDetail(val observatory: Observatory) : ObservatoryListSideEffect()
    object RequestLocationPermission : ObservatoryListSideEffect()
    object OpenLocationSettings : ObservatoryListSideEffect()
    data class ShowSnackbar(val message: String) : ObservatoryListSideEffect()
}