package com.udacity.astroapp.ui.screens.earth

import com.udacity.astroapp.models.EarthPhoto

data class EarthPhotoState(
    val photos: List<EarthPhoto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val selectedDate: String = "",
    val showDatePicker: Boolean = false,
    val gridMode: Boolean = true,
    val selectedPhoto: EarthPhoto? = null
)

sealed class EarthPhotoSideEffect {
    data class ShowError(val message: String) : EarthPhotoSideEffect()
    data class SharePhoto(val photo: EarthPhoto) : EarthPhotoSideEffect()
    data class OpenPhotoInFullscreen(val photo: EarthPhoto) : EarthPhotoSideEffect()
    object ShowDatePicker : EarthPhotoSideEffect()
}