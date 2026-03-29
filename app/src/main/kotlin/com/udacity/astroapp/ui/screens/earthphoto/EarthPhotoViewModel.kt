package com.udacity.astroapp.ui.screens.earthphoto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.repository.EarthPhotoRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class EarthPhotoViewModel(private val earthPhotoRepository: EarthPhotoRepository) :
    ViewModel(), ContainerHost<EarthPhotoState, EarthPhotoSideEffect> {

    override val container = container<EarthPhotoState, EarthPhotoSideEffect>(EarthPhotoState())

    init {
        observeEarthPhotos()
        loadPhotos()
    }

    private fun observeEarthPhotos() = intent {
        viewModelScope.launch {
            earthPhotoRepository.getAllEarthPhotos().collect { earthPhotos ->
                val filteredPhotos = filterByDate(earthPhotos, state.selectedDate)
                reduce {
                    state.copy(
                        allEarthPhotos = earthPhotos,
                        earthPhotos = filteredPhotos,
                        selectedPhoto = filteredPhotos.firstOrNull()
                    )
                }
            }
        }
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

        viewModelScope.launch {
            try {
                val (resolvedDate, _) =
                    earthPhotoRepository.getLatestAvailableEarthPhotos()
                reduce {
                    state.copy(
                        isLoading = false,
                        selectedDate = resolvedDate,
                        maxAvailableDate = resolvedDate
                    )
                }
            } catch (e: Exception) {
                reduce {
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load Earth photos"
                    )
                }
                postSideEffect(
                    EarthPhotoSideEffect.ShowError(e.message ?: "Failed to load Earth photos")
                )
            }
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

        // If no photos found for the selected date, try to fetch them
        if (filteredPhotos.isEmpty() && date.isNotBlank()) {
            reduce { state.copy(isLoading = true) }

            viewModelScope.launch {
                try {
                    earthPhotoRepository.getEarthPhotosByDate(date, forceRefresh = true)
                    reduce { state.copy(isLoading = false) }
                } catch (e: Exception) {
                    reduce {
                        state.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load Earth photos for selected date"
                        )
                    }
                }
            }
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
