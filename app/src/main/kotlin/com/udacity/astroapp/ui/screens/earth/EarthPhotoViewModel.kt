package com.udacity.astroapp.ui.screens.earth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.repository.EarthPhotoRepository
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class EarthPhotoViewModel(
    private val earthPhotoRepository: EarthPhotoRepository
) : ViewModel(), ContainerHost<EarthPhotoState, EarthPhotoSideEffect> {

    override val container = container<EarthPhotoState, EarthPhotoSideEffect>(EarthPhotoState())

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadTodaysEarthPhotos()
    }

    fun loadTodaysEarthPhotos() = intent {
        loadEarthPhotosByDate(LocalDate.now())
    }

    fun loadEarthPhotosByDate(date: LocalDate, forceRefresh: Boolean = false) = intent {
        reduce {
            state.copy(
                isLoading = true,
                error = null,
                selectedDate = date,
                isRefreshing = forceRefresh
            )
        }

        viewModelScope.launch {
            try {
                val photos = earthPhotoRepository.getEarthPhotosByDate(dateFormat.format(date), forceRefresh)
                reduce {
                    state.copy(
                        photos = photos,
                        isLoading = false,
                        isRefreshing = false,
                        error = if (photos.isEmpty()) "No Earth photos found for selected date" else null
                    )
                }
            } catch (e: Exception) {
                reduce {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load Earth photos"
                    )
                }
                postSideEffect(
                    EarthPhotoSideEffect.ShowError(
                        e.message ?: "Failed to load Earth photos"
                    )
                )
            }
        }
    }

    fun onDateSelected(date: LocalDate) = intent {
        reduce { state.copy(showDatePicker = false) }
        loadEarthPhotosByDate(date)
    }

    fun onGridModeToggled() = intent {
        reduce { state.copy(gridMode = !state.gridMode) }
    }

    fun onPhotoClicked(photo: com.udacity.astroapp.models.EarthPhoto) = intent {
        reduce { state.copy(selectedPhoto = photo) }
        postSideEffect(EarthPhotoSideEffect.OpenPhotoInFullscreen(photo))
    }

    fun onSharePhotoClicked(photo: com.udacity.astroapp.models.EarthPhoto) = intent {
        postSideEffect(EarthPhotoSideEffect.SharePhoto(photo))
    }

    fun onDatePickerClicked() = intent {
        reduce { state.copy(showDatePicker = true) }
    }

    fun onDatePickerDismissed() = intent {
        reduce { state.copy(showDatePicker = false) }
    }

    fun onRefresh() = intent {
        loadEarthPhotosByDate(state.selectedDate, forceRefresh = true)
    }

    fun onErrorDismissed() = intent {
        reduce { state.copy(error = null) }
    }

    fun onPhotoDetailsClosed() = intent {
        reduce { state.copy(selectedPhoto = null) }
    }
}