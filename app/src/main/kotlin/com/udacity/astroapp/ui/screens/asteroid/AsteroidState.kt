package com.udacity.astroapp.ui.screens.asteroid

import com.udacity.astroapp.models.Asteroid
import java.time.LocalDate

data class AsteroidState(
    val asteroids: List<Asteroid> = emptyList(),
    val filteredAsteroids: List<Asteroid> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val showHazardousOnly: Boolean = false,
    val sortBy: AsteroidSortOption = AsteroidSortOption.DATE,
    val selectedDate: LocalDate = LocalDate.now(),
)

enum class AsteroidSortOption {
    DATE, NAME, SIZE, VELOCITY
}

sealed class AsteroidSideEffect {
    data class ShowError(val message: String) : AsteroidSideEffect()
    data class OpenAsteroidUrl(val url: String) : AsteroidSideEffect()
}