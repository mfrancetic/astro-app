package com.udacity.astroapp.ui.screens.observatory

import androidx.lifecycle.ViewModel
import com.udacity.astroapp.repository.ObservatoryRepository
import kotlinx.coroutines.flow.collect
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
            observatoryRepository.loadObservatoryById(observatoryId).collect { observatory ->
                reduce { state.copy(isLoading = false, observatory = observatory, error = null) }
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
            if (observatory.observatoryUrl.isNotEmpty()) {
                postSideEffect(ObservatorySideEffect.OpenWebsite(observatory.observatoryUrl))
            }
        }
    }

    private fun callPhone() = intent {
        state.observatory?.let { observatory ->
            if (observatory.observatoryPhoneNumber.isNotEmpty()) {
                postSideEffect(ObservatorySideEffect.CallPhone(observatory.observatoryPhoneNumber))
            }
        }
    }

    private fun retry() = intent { loadObservatory(state.observatoryId) }
}
