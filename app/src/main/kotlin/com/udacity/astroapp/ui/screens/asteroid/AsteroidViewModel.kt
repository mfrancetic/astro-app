package com.udacity.astroapp.ui.screens.asteroid

import androidx.lifecycle.ViewModel
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.repository.AsteroidRepository
import kotlinx.coroutines.flow.collect
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class AsteroidViewModel(private val asteroidRepository: AsteroidRepository) :
    ViewModel(), ContainerHost<AsteroidState, AsteroidSideEffect> {

    override val container = container<AsteroidState, AsteroidSideEffect>(AsteroidState())

    init {
        loadAsteroids()
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

    fun loadAsteroids() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        try {
            asteroidRepository.loadAllAsteroids().collect { asteroids ->
                val filteredAsteroids =
                    applyFilters(
                        asteroids = asteroids,
                        selectedDate = state.selectedDate,
                        showHazardousOnly = state.showHazardousOnly
                    )
                reduce {
                    state.copy(
                        isLoading = false,
                        asteroids = asteroids,
                        filteredAsteroids = filteredAsteroids,
                        error = null
                    )
                }
            }
        } catch (e: Exception) {
            reduce {
                state.copy(isLoading = false, error = e.message ?: "Failed to load asteroids")
            }
            postSideEffect(AsteroidSideEffect.ShowError(e.message ?: "Failed to load asteroids"))
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
}
