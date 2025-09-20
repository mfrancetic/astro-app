package com.udacity.astroapp.ui.screens.marsphoto

import com.udacity.astroapp.models.MarsPhoto

data class MarsPhotoState(
    val isLoading: Boolean = false,
    val marsPhotos: List<MarsPhoto> = emptyList(),
    val selectedPhoto: MarsPhoto? = null,
    val selectedDate: String = "",
    val error: String? = null
)

sealed class MarsPhotoSideEffect {
    data class ShowError(val message: String) : MarsPhotoSideEffect()

    data class NavigateToDetail(val marsPhoto: MarsPhoto) : MarsPhotoSideEffect()

    object ShowDatePicker : MarsPhotoSideEffect()
}

sealed class MarsPhotoAction {
    object LoadPhotos : MarsPhotoAction()

    data class SelectDate(val date: String) : MarsPhotoAction()

    data class SelectPhoto(val marsPhoto: MarsPhoto) : MarsPhotoAction()

    object ShowDatePicker : MarsPhotoAction()

    object Retry : MarsPhotoAction()
}
