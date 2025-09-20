package com.udacity.astroapp.ui.screens.marsphoto

import com.udacity.astroapp.models.MarsPhoto
import java.time.LocalDate

data class MarsPhotoState(
    val isLoading: Boolean = false,
    val filteredPhotos: List<MarsPhoto> = emptyList(),
    val allMarsPhotos: List<MarsPhoto> = emptyList(),
    val selectedPhoto: MarsPhoto? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val error: String? = null
)

sealed class MarsPhotoSideEffect {
    data class ShowError(val message: String) : MarsPhotoSideEffect()
}
