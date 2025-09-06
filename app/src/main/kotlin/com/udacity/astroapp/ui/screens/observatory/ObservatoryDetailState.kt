package com.udacity.astroapp.ui.screens.observatory

import com.udacity.astroapp.models.Observatory

data class ObservatoryDetailState(
    val observatory: Observatory? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isMapReady: Boolean = false,
    val showFullMap: Boolean = false
)

sealed class ObservatoryDetailSideEffect {
    data class ShowError(val message: String) : ObservatoryDetailSideEffect()
    data class OpenWebsite(val url: String) : ObservatoryDetailSideEffect()
    data class CallPhoneNumber(val phoneNumber: String) : ObservatoryDetailSideEffect()
    data class OpenInMaps(val latitude: Double, val longitude: Double, val title: String) : ObservatoryDetailSideEffect()
    data class ShowSnackbar(val message: String) : ObservatoryDetailSideEffect()
}