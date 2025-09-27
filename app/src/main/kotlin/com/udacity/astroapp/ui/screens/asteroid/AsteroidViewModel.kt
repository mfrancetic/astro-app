package com.udacity.astroapp.ui.screens.asteroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.repository.AsteroidRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class AsteroidViewModel(private val asteroidRepository: AsteroidRepository) :
    ViewModel(), ContainerHost<AsteroidState, AsteroidSideEffect> {

    override val container = container<AsteroidState, AsteroidSideEffect>(AsteroidState())

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        observeAsteroids()
        loadAsteroids()
    }

    private fun observeAsteroids() = intent {
        viewModelScope.launch {
            asteroidRepository.getAllAsteroids().collect { asteroids ->
                reduce {
                    state.copy(
                        asteroids = asteroids,
                        filteredAsteroids =
                            filterAndSortAsteroids(
                                asteroids,
                                state.showHazardousOnly,
                                LocalDate.parse(state.selectedDate)
                            )
                    )
                }
            }
        }
    }

    fun handleAction(action: AsteroidAction) {
        when (action) {
            is AsteroidAction.LoadAsteroids -> loadAsteroids()
            is AsteroidAction.SelectDate -> selectDate(action.date)
            is AsteroidAction.SelectAsteroid -> selectAsteroid(action.asteroid)
            is AsteroidAction.FilterHazardous -> filterHazardous(action.showHazardousOnly)
            is AsteroidAction.ShowDatePicker -> showDatePicker()
            is AsteroidAction.Retry -> retry()
        }
    }

    fun loadAsteroids(forceRefresh: Boolean = true) = intent {
        reduce {
            state.copy(
                isLoading = true,
                error = null,
            )
        }

        viewModelScope.launch {
            try {
                asteroidRepository.refreshAsteroids(forceRefresh)
                reduce {
                    state.copy(
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                reduce {
                    state.copy(isLoading = false, error = e.message ?: "Failed to load asteroids")
                }
                postSideEffect(
                    AsteroidSideEffect.ShowError(e.message ?: "Failed to load asteroids")
                )
            }
        }
    }

    private fun selectDate(date: String) = intent {
        val filteredAsteroids =
            applyFilters(
                asteroids = state.asteroids,
                selectedDate = date,
                showHazardousOnly = state.showHazardousOnly
            )

        reduce { state.copy(selectedDate = date, filteredAsteroids = filteredAsteroids) }

        // If no asteroids found for the selected date, try to fetch them
        if (filteredAsteroids.isEmpty() && date.isNotBlank()) {
            reduce { state.copy(isLoading = true) }

            viewModelScope.launch {
                try {
                    asteroidRepository.fetchAsteroidsForDate(date)
                    reduce { state.copy(isLoading = false) }
                } catch (e: Exception) {
                    reduce {
                        state.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load asteroids for selected date"
                        )
                    }
                }
            }
        }
    }

    private fun selectAsteroid(asteroid: Asteroid) = intent {
        postSideEffect(AsteroidSideEffect.NavigateToDetail(asteroid))
    }

    private fun filterHazardous(showHazardousOnly: Boolean) = intent {
        val filteredAsteroids =
            applyFilters(
                asteroids = state.asteroids,
                selectedDate = state.selectedDate,
                showHazardousOnly = showHazardousOnly
            )
        reduce {
            state.copy(showHazardousOnly = showHazardousOnly, filteredAsteroids = filteredAsteroids)
        }
    }

    private fun showDatePicker() = intent { postSideEffect(AsteroidSideEffect.ShowDatePicker) }

    private fun retry() = intent { loadAsteroids() }

    private fun applyFilters(
        asteroids: List<Asteroid>,
        selectedDate: String,
        showHazardousOnly: Boolean
    ): List<Asteroid> {
        val dateFiltered =
            if (selectedDate.isNotBlank()) {
                asteroids.filter { it.asteroidApproachDate == selectedDate }
            } else {
                asteroids
            }

        return if (showHazardousOnly) {
            dateFiltered.filter { it.asteroidIsHazardous }
        } else {
            dateFiltered
        }
    }

    private fun filterAndSortAsteroids(
        asteroids: List<Asteroid>,
        showHazardousOnly: Boolean,
        date: LocalDate,
    ): List<Asteroid> {
        var filtered = asteroids.filter { it.asteroidApproachDate == date.format(dateFormat) }

        // Apply hazardous filter
        if (showHazardousOnly) {
            filtered = filtered.filter { it.asteroidIsHazardous }
        }

        // Apply sorting
        return filtered
    }
}
