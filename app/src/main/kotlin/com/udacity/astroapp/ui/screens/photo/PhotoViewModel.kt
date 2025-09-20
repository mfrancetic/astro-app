package com.udacity.astroapp.ui.screens.photo

import androidx.lifecycle.ViewModel
import com.udacity.astroapp.repository.PhotoRepository
import kotlinx.coroutines.flow.collect
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class PhotoViewModel(private val photoRepository: PhotoRepository) :
    ViewModel(), ContainerHost<PhotoState, PhotoSideEffect> {

    override val container = container<PhotoState, PhotoSideEffect>(PhotoState())

    init {
        loadPhotos()
    }

    fun handleAction(action: PhotoAction) {
        when (action) {
            is PhotoAction.LoadPhotos -> loadPhotos()
            is PhotoAction.SelectDate -> selectDate(action.date)
            is PhotoAction.SelectPhoto -> selectPhoto(action.photo)
            is PhotoAction.SharePhoto -> sharePhoto(action.photo)
            is PhotoAction.ShowDatePicker -> showDatePicker()
            is PhotoAction.Retry -> retry()
        }
    }

    fun loadPhotos() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        try {
            photoRepository.loadAllPhotos().collect { photos ->
                val filteredPhotos = filterByDate(photos, state.selectedDate)
                reduce {
                    state.copy(
                        isLoading = false,
                        photos = filteredPhotos,
                        allPhotos = photos,
                        selectedPhoto = filteredPhotos.firstOrNull(),
                        error = null
                    )
                }
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message ?: "Failed to load photos") }
            postSideEffect(PhotoSideEffect.ShowError(e.message ?: "Failed to load photos"))
        }
    }

    private fun selectDate(date: String) = intent {
        val filteredPhotos = filterByDate(state.allPhotos, date)
        reduce {
            state.copy(
                selectedDate = date,
                photos = filteredPhotos,
                selectedPhoto = filteredPhotos.firstOrNull()
            )
        }
    }

    private fun selectPhoto(photo: com.udacity.astroapp.models.Photo) = intent {
        reduce { state.copy(selectedPhoto = photo) }
        postSideEffect(PhotoSideEffect.NavigateToFullScreen(photo))
    }

    private fun sharePhoto(photo: com.udacity.astroapp.models.Photo) = intent {
        postSideEffect(PhotoSideEffect.SharePhoto(photo))
    }

    private fun showDatePicker() = intent { postSideEffect(PhotoSideEffect.ShowDatePicker) }

    private fun retry() = intent { loadPhotos() }

    private fun filterByDate(
        photos: List<com.udacity.astroapp.models.Photo>,
        date: String
    ): List<com.udacity.astroapp.models.Photo> {
        if (date.isBlank()) return photos

        return photos.filter { it.photoDate == date }
    }
}
