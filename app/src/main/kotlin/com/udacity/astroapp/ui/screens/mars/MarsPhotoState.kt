package com.udacity.astroapp.ui.screens.mars

import com.udacity.astroapp.models.MarsPhoto

data class MarsPhotoState(
    val photos: List<MarsPhoto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val selectedRover: String = "curiosity",
    val selectedSol: String = "1000",
    val selectedDate: String = "",
    val showDatePicker: Boolean = false,
    val availableRovers: List<String> = listOf("curiosity", "opportunity", "spirit")
)

sealed class MarsPhotoSideEffect {
    data class ShowError(val message: String) : MarsPhotoSideEffect()
    data class SharePhoto(val photo: MarsPhoto) : MarsPhotoSideEffect()
    data class OpenPhotoInFullscreen(val photo: MarsPhoto) : MarsPhotoSideEffect()
    object ShowDatePicker : MarsPhotoSideEffect()
}