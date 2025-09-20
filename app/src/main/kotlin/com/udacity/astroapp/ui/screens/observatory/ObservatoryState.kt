package com.udacity.astroapp.ui.screens.observatory

import com.udacity.astroapp.models.Observatory

// State for observatory list screen
data class ObservatoryListState(
    val isLoading: Boolean = false,
    val observatories: List<Observatory> = emptyList(),
    val filteredObservatories: List<Observatory> = emptyList(),
    val searchQuery: String = "",
    val selectedObservatory: Observatory? = null,
    val error: String? = null
)

// State for observatory detail screen
data class ObservatoryDetailState(
    val isLoading: Boolean = false,
    val observatory: Observatory? = null,
    val observatoryId: String = "",
    val error: String? = null
)

sealed class ObservatorySideEffect {
    data class ShowError(val message: String) : ObservatorySideEffect()

    data class NavigateToDetail(val observatoryId: String) : ObservatorySideEffect()

    data class NavigateToMaps(val observatory: Observatory) : ObservatorySideEffect()

    data class OpenWebsite(val url: String) : ObservatorySideEffect()

    data class CallPhone(val phoneNumber: String) : ObservatorySideEffect()

    object NavigateBack : ObservatorySideEffect()
}

// Actions for observatory list
sealed class ObservatoryListAction {
    object LoadObservatories : ObservatoryListAction()

    data class SearchObservatories(val query: String) : ObservatoryListAction()

    data class SelectObservatory(val observatoryId: String) : ObservatoryListAction()

    object Retry : ObservatoryListAction()
}

// Actions for observatory detail
sealed class ObservatoryDetailAction {
    data class LoadObservatory(val observatoryId: String) : ObservatoryDetailAction()

    object NavigateToMaps : ObservatoryDetailAction()

    object OpenWebsite : ObservatoryDetailAction()

    object CallPhone : ObservatoryDetailAction()

    object Retry : ObservatoryDetailAction()
}
