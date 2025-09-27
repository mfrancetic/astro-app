package com.udacity.astroapp.ui.screens.photo

import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.utils.DateUtils

data class PhotoState(
    val isLoading: Boolean = false,
    val photos: List<Photo> = emptyList(),
    val allPhotos: List<Photo> = emptyList(),
    val selectedPhoto: Photo? = null,
    val selectedDate: String = DateUtils.todayIsoDate(),
    val error: String? = null
)

sealed class PhotoSideEffect {
    data class ShowError(val message: String) : PhotoSideEffect()

    data class SharePhoto(val photo: Photo) : PhotoSideEffect()

    object ShowDatePicker : PhotoSideEffect()
}

sealed class PhotoAction {
    object LoadPhotos : PhotoAction()

    data class SelectDate(val date: String) : PhotoAction()

    data class SharePhoto(val photo: Photo) : PhotoAction()

    object ShowDatePicker : PhotoAction()

    object Retry : PhotoAction()
}
