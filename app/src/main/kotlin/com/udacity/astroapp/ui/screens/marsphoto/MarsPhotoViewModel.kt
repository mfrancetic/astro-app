package com.udacity.astroapp.ui.screens.marsphoto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.repository.MarsPhotoRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class MarsPhotoViewModel(private val marsPhotoRepository: MarsPhotoRepository) :
    ViewModel(), ContainerHost<MarsPhotoState, MarsPhotoSideEffect> {

    override val container = container<MarsPhotoState, MarsPhotoSideEffect>(MarsPhotoState())

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        intent { loadMarsPhotosByDate(state.selectedDate) }
    }

    fun loadMarsPhotosByDate(date: LocalDate, forceRefresh: Boolean = false) = intent {
        reduce {
            state.copy(
                isLoading = true,
                error = null,
                selectedDate = date,
            )
        }

        viewModelScope.launch {
            try {
                val photos =
                    marsPhotoRepository.getMarsPhotosByDate(date.format(dateFormat), forceRefresh)
                reduce {
                    state.copy(
                        filteredPhotos = photos,
                        isLoading = false,
                        error = if (photos.isEmpty()) "No photos found for date $date" else null
                    )
                }
            } catch (e: Exception) {
                reduce {
                    state.copy(isLoading = false, error = e.message ?: "Failed to load Mars photos")
                }
                postSideEffect(
                    MarsPhotoSideEffect.ShowError(e.message ?: "Failed to load Mars photos")
                )
            }
        }
    }

    fun onDateSelected(date: LocalDate) = intent { loadMarsPhotosByDate(date) }

    fun onRefresh() = intent { loadMarsPhotosByDate(state.selectedDate, forceRefresh = true) }
}
