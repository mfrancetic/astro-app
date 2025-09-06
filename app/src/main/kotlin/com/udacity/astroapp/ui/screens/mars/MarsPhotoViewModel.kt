package com.udacity.astroapp.ui.screens.mars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.repository.MarsPhotoRepository
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MarsPhotoViewModel(
    private val marsPhotoRepository: MarsPhotoRepository
) : ViewModel(), ContainerHost<MarsPhotoState, MarsPhotoSideEffect> {

    override val container = container<MarsPhotoState, MarsPhotoSideEffect>(MarsPhotoState())

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadMarsPhotosBySol(container.stateFlow.value.selectedSol)
    }

    fun loadMarsPhotosBySol(sol: String, forceRefresh: Boolean = false) = intent {
        reduce {
            state.copy(
                isLoading = true,
                error = null,
                selectedSol = sol,
                isRefreshing = forceRefresh
            )
        }

        viewModelScope.launch {
            try {
                val photos = marsPhotoRepository.getMarsPhotosBySol(sol, forceRefresh)
                reduce {
                    state.copy(
                        photos = photos,
                        isLoading = false,
                        isRefreshing = false,
                        error = if (photos.isEmpty()) "No photos found for Sol $sol" else null
                    )
                }
            } catch (e: Exception) {
                reduce {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load Mars photos"
                    )
                }
                postSideEffect(
                    MarsPhotoSideEffect.ShowError(
                        e.message ?: "Failed to load Mars photos"
                    )
                )
            }
        }
    }

    fun loadMarsPhotosByDate(date: LocalDate, forceRefresh: Boolean = false) = intent {
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
                val photos = marsPhotoRepository.getMarsPhotosByDate(date.format(dateFormat), forceRefresh)
                reduce {
                    state.copy(
                        photos = photos,
                        isLoading = false,
                        isRefreshing = false,
                        error = if (photos.isEmpty()) "No photos found for date $date" else null
                    )
                }
            } catch (e: Exception) {
                reduce {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load Mars photos"
                    )
                }
                postSideEffect(
                    MarsPhotoSideEffect.ShowError(
                        e.message ?: "Failed to load Mars photos"
                    )
                )
            }
        }
    }

    fun onRoverSelected(rover: String) = intent {
        reduce { state.copy(selectedRover = rover) }
        // Note: Would need to update the API service to support different rovers
        loadMarsPhotosBySol(state.selectedSol)
    }

    fun onSolChanged(sol: String) = intent {
        if (sol.isNotBlank()) {
            loadMarsPhotosBySol(sol)
        }
    }

    fun onDateSelected(date: LocalDate) = intent {
        reduce { state.copy(showDatePicker = false) }
        loadMarsPhotosByDate(date)
    }

    fun onSharePhotoClicked(photo: com.udacity.astroapp.models.MarsPhoto) = intent {
        postSideEffect(MarsPhotoSideEffect.SharePhoto(photo))
    }

    fun onPhotoClicked(photo: com.udacity.astroapp.models.MarsPhoto) = intent {
        postSideEffect(MarsPhotoSideEffect.OpenPhotoInFullscreen(photo))
    }

    fun onDatePickerClicked() = intent {
        reduce { state.copy(showDatePicker = true) }
    }

    fun onDatePickerDismissed() = intent {
        reduce { state.copy(showDatePicker = false) }
    }

    fun onRefresh() = intent {
        if (state.selectedSol.isNotBlank()) {
            loadMarsPhotosBySol(state.selectedSol, forceRefresh = true)
        } else {
            loadMarsPhotosByDate(state.selectedDate, forceRefresh = true)
        }
    }

    fun onErrorDismissed() = intent {
        reduce { state.copy(error = null) }
    }
}