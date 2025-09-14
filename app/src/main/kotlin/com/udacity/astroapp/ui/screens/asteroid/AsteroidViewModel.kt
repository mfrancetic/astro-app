package com.udacity.astroapp.ui.screens.asteroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.repository.AsteroidRepository
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class AsteroidViewModel(
    private val asteroidRepository: AsteroidRepository
) : ViewModel(), ContainerHost<AsteroidState, AsteroidSideEffect> {

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

    private fun loadAsteroids() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        try {
            asteroidRepository.loadAllAsteroids().observeForever { asteroids ->
                viewModelScope.launch {
                    intent {
                        val filteredAsteroids = if (state.showHazardousOnly) {
                            asteroids?.filter { it.asteroidIsHazardous } ?: emptyList()
                        } else {
                            asteroids ?: emptyList()
                        }

                        reduce {
                            state.copy(
                                isLoading = false,
                                asteroids = asteroids ?: emptyList(),
                                filteredAsteroids = filteredAsteroids,
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
                    error = e.message ?: "Failed to load asteroids"
                )
            }
            postSideEffect(AsteroidSideEffect.ShowError(e.message ?: "Failed to load asteroids"))
        }
    }

    private fun selectDate(date: String) = intent {
        reduce { state.copy(selectedDate = date) }
        // Filter asteroids by selected date
        filterAsteroidsByDate(date)
    }

    private fun filterAsteroidsByDate(date: String) = intent {
        val filteredByDate = state.asteroids.filter { asteroid ->
            asteroid.asteroidApproachDate == date
        }

        val finalFiltered = if (state.showHazardousOnly) {
            filteredByDate.filter { it.asteroidIsHazardous }
        } else {
            filteredByDate
        }

        reduce { state.copy(filteredAsteroids = finalFiltered) }
    }

    private fun selectAsteroid(asteroid: com.udacity.astroapp.models.Asteroid) = intent {
        postSideEffect(AsteroidSideEffect.NavigateToDetail(asteroid))
    }

    private fun filterHazardous(showHazardousOnly: Boolean) = intent {
        reduce { state.copy(showHazardousOnly = showHazardousOnly) }

        val filteredAsteroids = if (showHazardousOnly) {
            state.asteroids.filter { it.asteroidIsHazardous }
        } else {
            state.asteroids
        }

        reduce { state.copy(filteredAsteroids = filteredAsteroids) }
    }

    private fun showDatePicker() = intent {
        postSideEffect(AsteroidSideEffect.ShowDatePicker)
    }

    private fun retry() = intent {
        loadAsteroids()
    }
}