package com.udacity.astroapp.ui.screens.asteroid

import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.utils.DateUtils

data class AsteroidState(
    val isLoading: Boolean = false,
    val asteroids: List<Asteroid> = emptyList(),
    val filteredAsteroids: List<Asteroid> = emptyList(),
    val selectedDate: String = DateUtils.todayIsoDate(),
    val showHazardousOnly: Boolean = false,
    val error: String? = null
)

sealed class AsteroidSideEffect {
    data class ShowError(val message: String) : AsteroidSideEffect()

    data class NavigateToDetail(val asteroid: Asteroid) : AsteroidSideEffect()

    object ShowDatePicker : AsteroidSideEffect()
}

sealed class AsteroidAction {
    object LoadAsteroids : AsteroidAction()

    data class SelectDate(val date: String) : AsteroidAction()

    data class SelectAsteroid(val asteroid: Asteroid) : AsteroidAction()

    data class FilterHazardous(val showHazardousOnly: Boolean) : AsteroidAction()

    object ShowDatePicker : AsteroidAction()

    object Retry : AsteroidAction()
}
