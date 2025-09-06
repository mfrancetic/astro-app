package com.udacity.astroapp.ui.screens.observatory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.repository.ObservatoryRepository
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class ObservatoryDetailViewModel(
    private val observatoryRepository: ObservatoryRepository,
    private val observatoryId: String
) : ViewModel(), ContainerHost<ObservatoryDetailState, ObservatoryDetailSideEffect> {

    override val container = container<ObservatoryDetailState, ObservatoryDetailSideEffect>(ObservatoryDetailState())

    init {
        loadObservatory()
    }

    private fun loadObservatory() = intent {
        reduce { 
            state.copy(
                isLoading = true, 
                error = null
            ) 
        }

        viewModelScope.launch {
            try {
                val observatory = observatoryRepository.getObservatoryById(observatoryId)
                reduce {
                    state.copy(
                        observatory = observatory,
                        isLoading = false,
                        error = if (observatory == null) "Observatory not found" else null
                    )
                }
            } catch (e: Exception) {
                reduce {
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load observatory details"
                    )
                }
                postSideEffect(ObservatoryDetailSideEffect.ShowError(e.message ?: "Failed to load observatory details"))
            }
        }
    }

    fun onWebsiteClicked() = intent {
        state.observatory?.observatoryUrl?.let { url ->
            if (url.isNotBlank()) {
                postSideEffect(ObservatoryDetailSideEffect.OpenWebsite(url))
            }
        }
    }

    fun onPhoneClicked() = intent {
        state.observatory?.observatoryPhoneNumber?.let { phoneNumber ->
            if (phoneNumber.isNotBlank()) {
                postSideEffect(ObservatoryDetailSideEffect.CallPhoneNumber(phoneNumber))
            }
        }
    }

    fun onMapClicked() = intent {
        state.observatory?.let { observatory ->
            postSideEffect(
                ObservatoryDetailSideEffect.OpenInMaps(
                    latitude = observatory.observatoryLatitude,
                    longitude = observatory.observatoryLongitude,
                    title = observatory.observatoryName ?: "Observatory"
                )
            )
        }
    }

    fun onMapReady() = intent {
        reduce { state.copy(isMapReady = true) }
    }

    fun onFullMapToggled() = intent {
        reduce { state.copy(showFullMap = !state.showFullMap) }
    }

    fun onErrorDismissed() = intent {
        reduce { state.copy(error = null) }
    }

    fun refresh() = intent {
        loadObservatory()
    }
}