package com.udacity.astroapp.ui.screens.observatory

import androidx.lifecycle.ViewModel
import com.udacity.astroapp.repository.ObservatoryRepository
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class ObservatoryDetailViewModel(
    private val observatoryRepository: ObservatoryRepository,
    private val observatoryId: String
) : ViewModel(), ContainerHost<ObservatoryDetailState, ObservatorySideEffect> {

    override val container =
        container<ObservatoryDetailState, ObservatorySideEffect>(
            ObservatoryDetailState(observatoryId = observatoryId)
        )

    init {
        loadObservatory(observatoryId)
    }

    fun handleAction(action: ObservatoryDetailAction) {
        when (action) {
            is ObservatoryDetailAction.LoadObservatory -> loadObservatory(action.observatoryId)
            is ObservatoryDetailAction.NavigateToMaps -> navigateToMaps()
            is ObservatoryDetailAction.OpenWebsite -> openWebsite()
            is ObservatoryDetailAction.CallPhone -> callPhone()
            is ObservatoryDetailAction.Retry -> retry()
        }
    }

    fun loadObservatory(observatoryId: String) = intent {
        reduce { state.copy(isLoading = true, error = null, observatoryId = observatoryId) }

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
                state.copy(isLoading = false, error = e.message ?: "Failed to load observatory")
            }
            postSideEffect(
                ObservatorySideEffect.ShowError(e.message ?: "Failed to load observatory")
            )
        }
    }

    private fun navigateToMaps() = intent {
        state.observatory?.let { observatory ->
            postSideEffect(ObservatorySideEffect.NavigateToMaps(observatory))
        }
    }

    private fun openWebsite() = intent {
        state.observatory?.let { observatory ->
            if (observatory.observatoryUrl?.isNotEmpty() == true) {
                postSideEffect(ObservatorySideEffect.OpenWebsite(observatory.observatoryUrl))
            }
        }
    }

    private fun callPhone() = intent {
        state.observatory?.let { observatory ->
            if (observatory.observatoryPhoneNumber?.isNotEmpty() == true) {
                postSideEffect(ObservatorySideEffect.CallPhone(observatory.observatoryPhoneNumber))
            }
        }
    }

    private fun retry() = intent { loadObservatory(state.observatoryId) }
}
