package com.udacity.astroapp.ui.screens.marsphoto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.repository.MarsPhotoRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class MarsPhotoViewModel(
    private val marsPhotoRepository: MarsPhotoRepository
) : ViewModel(), ContainerHost<MarsPhotoState, MarsPhotoSideEffect> {

    override val container = container<MarsPhotoState, MarsPhotoSideEffect>(MarsPhotoState())

    init {
        loadPhotos()
    }

    fun handleAction(action: MarsPhotoAction) {
        when (action) {
            is MarsPhotoAction.LoadPhotos -> loadPhotos()
            is MarsPhotoAction.SelectDate -> selectDate(action.date)
            is MarsPhotoAction.SelectPhoto -> selectPhoto(action.marsPhoto)
            is MarsPhotoAction.ShowDatePicker -> showDatePicker()
            is MarsPhotoAction.Retry -> retry()
        }
    }

    fun loadPhotos() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        try {
            marsPhotoRepository.loadAllMarsPhotos().collect { marsPhotos ->
                reduce {
                    state.copy(
                        isLoading = false,
                        marsPhotos = marsPhotos,
                        error = null
                    )
                }
            }
        } catch (e: Exception) {
            reduce {
                state.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load Mars photos"
                )
            }
            postSideEffect(MarsPhotoSideEffect.ShowError(e.message ?: "Failed to load Mars photos"))
        }
    }

    private fun selectDate(date: String) = intent {
        reduce { state.copy(selectedDate = date) }
        // Trigger photo loading for the selected date
        loadPhotos()
    }

    private fun selectPhoto(marsPhoto: com.udacity.astroapp.models.MarsPhoto) = intent {
        reduce { state.copy(selectedPhoto = marsPhoto) }
        postSideEffect(MarsPhotoSideEffect.NavigateToDetail(marsPhoto))
    }

    private fun showDatePicker() = intent {
        postSideEffect(MarsPhotoSideEffect.ShowDatePicker)
    }

    private fun retry() = intent {
        loadPhotos()
    }
}