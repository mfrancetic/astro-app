package com.udacity.astroapp.ui.screens.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.repository.PhotoRepository
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PhotoViewModel(
    private val photoRepository: PhotoRepository
) : ViewModel(), ContainerHost<PhotoState, PhotoSideEffect> {

    override val container = container<PhotoState, PhotoSideEffect>(PhotoState())

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadTodaysPhoto()
    }

    fun loadTodaysPhoto() = intent {
        loadPhotoByDate(LocalDate.now())
    }

    fun loadPhotoByDate(date: LocalDate, forceRefresh: Boolean = false) = intent {
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
                val photo = photoRepository.getPhotoByDate(date.format(dateFormat), forceRefresh)
                reduce {
                    state.copy(
                        photo = photo,
                        isLoading = false,
                        isRefreshing = false,
                        error = if (photo == null) "No photo found for selected date" else null
                    )
                }
            } catch (e: Exception) {
                reduce {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load photo"
                    )
                }
                postSideEffect(PhotoSideEffect.ShowError(e.message ?: "Failed to load photo"))
            }
        }
    }

    fun onDateSelected(date: LocalDate) = intent {
        reduce { state.copy(showDatePicker = false) }
        loadPhotoByDate(date)
    }

    fun onSharePhotoClicked() = intent {
        state.photo?.let { photo ->
            postSideEffect(PhotoSideEffect.SharePhoto(photo))
        }
    }

    fun onPhotoClicked() = intent {
        state.photo?.let { photo ->
            postSideEffect(PhotoSideEffect.OpenPhotoInFullscreen(photo))
        }
    }

    fun onDatePickerClicked() = intent {
        reduce { state.copy(showDatePicker = true) }
    }

    fun onDatePickerDismissed() = intent {
        reduce { state.copy(showDatePicker = false) }
    }

    fun onRefresh() = intent {
        loadPhotoByDate(state.selectedDate, forceRefresh = true)
    }

    fun onErrorDismissed() = intent {
        reduce { state.copy(error = null) }
    }
}