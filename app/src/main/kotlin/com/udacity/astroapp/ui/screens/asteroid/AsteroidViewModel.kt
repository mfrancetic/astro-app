package com.udacity.astroapp.ui.screens.asteroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.repository.AsteroidRepository
import kotlinx.coroutines.flow.collect
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
        observeAsteroids()
        loadAsteroids()
    }

    private fun observeAsteroids() = intent {
        viewModelScope.launch {
            asteroidRepository.getAllAsteroids().collect { asteroids ->
                reduce {
                    state.copy(
                        asteroids = asteroids,
                        filteredAsteroids = filterAndSortAsteroids(asteroids, state.searchQuery, state.showHazardousOnly, state.sortBy)
                    )
                }
            }
        }
    }

    fun loadAsteroids(forceRefresh: Boolean = false) = intent {
        reduce { 
            state.copy(
                isLoading = true, 
                error = null,
                isRefreshing = forceRefresh
            ) 
        }

        viewModelScope.launch {
            try {
                asteroidRepository.refreshAsteroids(forceRefresh)
                reduce {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                reduce {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load asteroids"
                    )
                }
                postSideEffect(AsteroidSideEffect.ShowError(e.message ?: "Failed to load asteroids"))
            }
        }
    }

    fun onSearchQueryChanged(query: String) = intent {
        reduce { 
            state.copy(
                searchQuery = query,
                filteredAsteroids = filterAndSortAsteroids(state.asteroids, query, state.showHazardousOnly, state.sortBy)
            )
        }
    }

    fun onHazardousFilterToggled() = intent {
        val newShowHazardous = !state.showHazardousOnly
        reduce { 
            state.copy(
                showHazardousOnly = newShowHazardous,
                filteredAsteroids = filterAndSortAsteroids(state.asteroids, state.searchQuery, newShowHazardous, state.sortBy)
            )
        }
    }

    fun onSortOptionChanged(sortOption: AsteroidSortOption) = intent {
        reduce { 
            state.copy(
                sortBy = sortOption,
                filteredAsteroids = filterAndSortAsteroids(state.asteroids, state.searchQuery, state.showHazardousOnly, sortOption)
            )
        }
    }

    fun onAsteroidClicked(asteroid: Asteroid) = intent {
        postSideEffect(AsteroidSideEffect.ShowAsteroidDetails(asteroid))
    }

    fun onAsteroidUrlClicked(url: String) = intent {
        postSideEffect(AsteroidSideEffect.OpenAsteroidUrl(url))
    }

    fun onRefresh() = intent {
        loadAsteroids(forceRefresh = true)
    }

    fun onErrorDismissed() = intent {
        reduce { state.copy(error = null) }
    }

    private fun filterAndSortAsteroids(
        asteroids: List<Asteroid>,
        searchQuery: String,
        showHazardousOnly: Boolean,
        sortBy: AsteroidSortOption
    ): List<Asteroid> {
        var filtered = asteroids

        // Apply search filter
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { 
                it.asteroidName.contains(searchQuery, ignoreCase = true)
            }
        }

        // Apply hazardous filter
        if (showHazardousOnly) {
            filtered = filtered.filter { it.asteroidIsHazardous }
        }

        // Apply sorting
        return when (sortBy) {
            AsteroidSortOption.DATE -> filtered.sortedBy { it.asteroidApproachDate }
            AsteroidSortOption.NAME -> filtered.sortedBy { it.asteroidName }
            AsteroidSortOption.SIZE -> filtered.sortedByDescending { it.asteroidDiameterMax }
            AsteroidSortOption.VELOCITY -> filtered.sortedByDescending { 
                it.asteroidVelocity.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0 
            }
        }
    }
}