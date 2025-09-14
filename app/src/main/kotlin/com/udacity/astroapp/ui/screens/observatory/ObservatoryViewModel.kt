package com.udacity.astroapp.ui.screens.observatory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.repository.ObservatoryRepository
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class ObservatoryViewModel(
    private val observatoryRepository: ObservatoryRepository
) : ViewModel(), ContainerHost<ObservatoryListState, ObservatorySideEffect> {

    override val container = container<ObservatoryListState, ObservatorySideEffect>(ObservatoryListState())

    init {
        loadObservatories()
    }

    fun handleAction(action: ObservatoryListAction) {
        when (action) {
            is ObservatoryListAction.LoadObservatories -> loadObservatories()
            is ObservatoryListAction.SearchObservatories -> searchObservatories(action.query)
            is ObservatoryListAction.SelectObservatory -> selectObservatory(action.observatoryId)
            is ObservatoryListAction.Retry -> retry()
        }
    }

    private fun loadObservatories() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        try {
            observatoryRepository.loadAllObservatories().observeForever { observatories ->
                viewModelScope.launch {
                    intent {
                        reduce {
                            state.copy(
                                isLoading = false,
                                observatories = observatories ?: emptyList(),
                                filteredObservatories = observatories ?: emptyList(),
                                error = null
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            reduce {
                state.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load observatories"
                )
            }
            postSideEffect(ObservatorySideEffect.ShowError(e.message ?: "Failed to load observatories"))
        }
    }

    private fun searchObservatories(query: String) = intent {
        reduce { state.copy(searchQuery = query) }

        val filteredObservatories = if (query.isEmpty()) {
            state.observatories
        } else {
            state.observatories.filter { observatory ->
                observatory.observatoryName.contains(query, ignoreCase = true) ||
                        observatory.observatoryAddress.contains(query, ignoreCase = true)
            }
        }

        reduce { state.copy(filteredObservatories = filteredObservatories) }
    }

    private fun selectObservatory(observatoryId: String) = intent {
        val selectedObservatory = state.observatories.find { it.observatoryId == observatoryId }
        if (selectedObservatory != null) {
            reduce { state.copy(selectedObservatory = selectedObservatory) }
            postSideEffect(ObservatorySideEffect.NavigateToDetail(observatoryId))
        }
    }

    private fun retry() = intent {
        loadObservatories()
    }
}