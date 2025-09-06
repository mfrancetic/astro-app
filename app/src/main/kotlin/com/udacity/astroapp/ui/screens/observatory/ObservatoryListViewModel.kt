package com.udacity.astroapp.ui.screens.observatory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.repository.ObservatoryRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class ObservatoryListViewModel(
    private val observatoryRepository: ObservatoryRepository
) : ViewModel(), ContainerHost<ObservatoryListState, ObservatoryListSideEffect> {

    override val container = container<ObservatoryListState, ObservatoryListSideEffect>(ObservatoryListState())

    init {
        observeObservatories()
        loadObservatories()
    }

    private fun observeObservatories() = intent {
        observatoryRepository.getAllObservatories()
            .onEach { observatories ->
                reduce {
                    state.copy(
                        observatories = observatories,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
            .catch { exception ->
                reduce {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = exception.message ?: "Failed to load observatories"
                    )
                }
                postSideEffect(ObservatoryListSideEffect.ShowError(exception.message ?: "Failed to load observatories"))
            }
            .collect()
    }

    fun loadObservatories(forceRefresh: Boolean = false) = intent {
        reduce { 
            state.copy(
                isLoading = !forceRefresh,
                isRefreshing = forceRefresh,
                error = null
            ) 
        }

        viewModelScope.launch {
            try {
                val location = state.currentLocation
                observatoryRepository.refreshObservatories(
                    latitude = location?.first,
                    longitude = location?.second,
                    forceRefresh = forceRefresh
                )
            } catch (e: Exception) {
                reduce {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to refresh observatories"
                    )
                }
                postSideEffect(ObservatoryListSideEffect.ShowError(e.message ?: "Failed to refresh observatories"))
            }
        }
    }

    fun onObservatoryClicked(observatory: Observatory) = intent {
        postSideEffect(ObservatoryListSideEffect.NavigateToObservatoryDetail(observatory))
    }

    fun onLocationPermissionGranted() = intent {
        reduce { state.copy(hasLocationPermission = true) }
        // Request current location and reload
        loadObservatories(forceRefresh = true)
    }

    fun onLocationPermissionDenied() = intent {
        reduce { 
            state.copy(
                hasLocationPermission = false,
                showLocationPermissionDialog = false
            ) 
        }
        postSideEffect(ObservatoryListSideEffect.ShowSnackbar("Location permission is required for better observatory recommendations"))
        // Still load observatories without location
        loadObservatories()
    }

    fun onLocationPermissionDialogDismissed() = intent {
        reduce { state.copy(showLocationPermissionDialog = false) }
    }

    fun onLocationSettingsDialogDismissed() = intent {
        reduce { state.copy(showLocationSettingsDialog = false) }
    }

    fun onLocationUpdated(latitude: Double, longitude: Double) = intent {
        reduce { 
            state.copy(
                currentLocation = Pair(latitude, longitude),
                isLocationEnabled = true
            ) 
        }
        // Refresh observatories with new location
        loadObservatories(forceRefresh = true)
    }

    fun onLocationUnavailable() = intent {
        reduce { 
            state.copy(
                isLocationEnabled = false,
                showLocationSettingsDialog = true
            ) 
        }
    }

    fun onRefresh() = intent {
        loadObservatories(forceRefresh = true)
    }

    fun onErrorDismissed() = intent {
        reduce { state.copy(error = null) }
    }

    fun requestLocationPermission() = intent {
        postSideEffect(ObservatoryListSideEffect.RequestLocationPermission)
    }

    fun openLocationSettings() = intent {
        postSideEffect(ObservatoryListSideEffect.OpenLocationSettings)
    }
}