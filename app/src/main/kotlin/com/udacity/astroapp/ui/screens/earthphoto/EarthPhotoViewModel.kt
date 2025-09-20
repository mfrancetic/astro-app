package com.udacity.astroapp.ui.screens.earthphoto

import androidx.lifecycle.ViewModel
import com.udacity.astroapp.repository.EarthPhotoRepository
import kotlinx.coroutines.flow.collect
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class EarthPhotoViewModel(private val earthPhotoRepository: EarthPhotoRepository) :
    ViewModel(), ContainerHost<EarthPhotoState, EarthPhotoSideEffect> {

    override val container = container<EarthPhotoState, EarthPhotoSideEffect>(EarthPhotoState())

    init {
        loadPhotos()
    }

    fun handleAction(action: EarthPhotoAction) {
        when (action) {
            is EarthPhotoAction.LoadPhotos -> loadPhotos()
            is EarthPhotoAction.SelectDate -> selectDate(action.date)
            is EarthPhotoAction.SelectPhoto -> selectPhoto(action.earthPhoto)
            is EarthPhotoAction.ShowDatePicker -> showDatePicker()
            is EarthPhotoAction.Retry -> retry()
        }
    }

    fun loadPhotos() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        try {
            earthPhotoRepository.getAllEarthPhotos().collect { earthPhotos ->
                val filteredPhotos = filterByDate(earthPhotos, state.selectedDate)
                reduce {
                    state.copy(
                        isLoading = false,
                        allEarthPhotos = earthPhotos,
                        earthPhotos = filteredPhotos,
                        selectedPhoto = filteredPhotos.firstOrNull(),
                        error = null
                    )
                }
            }
        } catch (e: Exception) {
            reduce {
                state.copy(isLoading = false, error = e.message ?: "Failed to load Earth photos")
            }
            postSideEffect(
                EarthPhotoSideEffect.ShowError(e.message ?: "Failed to load Earth photos")
            )
        }
    }

    private fun selectDate(date: String) = intent {
        val filteredPhotos = filterByDate(state.allEarthPhotos, date)
        reduce {
            state.copy(
                selectedDate = date,
                earthPhotos = filteredPhotos,
                selectedPhoto = filteredPhotos.firstOrNull()
            )
        }
    }

    private fun selectPhoto(earthPhoto: com.udacity.astroapp.models.EarthPhoto) = intent {
        reduce { state.copy(selectedPhoto = earthPhoto) }
        postSideEffect(EarthPhotoSideEffect.NavigateToDetail(earthPhoto))
    }

    private fun showDatePicker() = intent { postSideEffect(EarthPhotoSideEffect.ShowDatePicker) }

    private fun retry() = intent { loadPhotos() }

    private fun filterByDate(
        photos: List<com.udacity.astroapp.models.EarthPhoto>,
        date: String
    ): List<com.udacity.astroapp.models.EarthPhoto> {
        if (date.isBlank()) return photos

        return photos.filter { earthPhoto -> earthPhoto.earthPhotoDateTime.startsWith(date) }
    }
}
