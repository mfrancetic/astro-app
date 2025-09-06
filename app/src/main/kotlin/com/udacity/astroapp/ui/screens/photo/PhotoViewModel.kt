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
import java.text.SimpleDateFormat
import java.util.*

class PhotoViewModel(
    private val photoRepository: PhotoRepository
) : ViewModel(), ContainerHost<PhotoState, PhotoSideEffect> {

    override val container = container<PhotoState, PhotoSideEffect>(PhotoState())

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadTodaysPhoto()
    }

    fun loadTodaysPhoto() = intent {
        val todayDate = dateFormat.format(Date())
        loadPhotoByDate(todayDate)
    }

    fun loadPhotoByDate(date: String, forceRefresh: Boolean = false) = intent {
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
                val photo = photoRepository.getPhotoByDate(date, forceRefresh)
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

    fun onDateSelected(date: String) = intent {
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