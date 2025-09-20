package com.udacity.astroapp.ui.screens.earthphoto

import com.udacity.astroapp.models.EarthPhoto
import com.udacity.astroapp.utils.DateUtils

data class EarthPhotoState(
    val isLoading: Boolean = false,
    val earthPhotos: List<EarthPhoto> = emptyList(),
    val allEarthPhotos: List<EarthPhoto> = emptyList(),
    val selectedPhoto: EarthPhoto? = null,
    val selectedDate: String = DateUtils.todayIsoDate(),
    val error: String? = null
)

sealed class EarthPhotoSideEffect {
    data class ShowError(val message: String) : EarthPhotoSideEffect()

    data class NavigateToDetail(val earthPhoto: EarthPhoto) : EarthPhotoSideEffect()

    object ShowDatePicker : EarthPhotoSideEffect()
}

sealed class EarthPhotoAction {
    object LoadPhotos : EarthPhotoAction()

    data class SelectDate(val date: String) : EarthPhotoAction()

    data class SelectPhoto(val earthPhoto: EarthPhoto) : EarthPhotoAction()

    object ShowDatePicker : EarthPhotoAction()

    object Retry : EarthPhotoAction()
}
