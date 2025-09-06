package com.udacity.astroapp.ui.screens.photo

import com.udacity.astroapp.models.Photo

data class PhotoState(
    val photo: Photo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDate: String = "",
    val isRefreshing: Boolean = false,
    val showDatePicker: Boolean = false
)

sealed class PhotoSideEffect {
    data class ShowError(val message: String) : PhotoSideEffect()
    data class SharePhoto(val photo: Photo) : PhotoSideEffect()
    data class OpenPhotoInFullscreen(val photo: Photo) : PhotoSideEffect()
    object ShowDatePicker : PhotoSideEffect()
}