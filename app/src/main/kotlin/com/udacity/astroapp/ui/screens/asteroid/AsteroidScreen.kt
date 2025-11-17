package com.udacity.astroapp.ui.screens.asteroid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.ui.components.AsteroidFilterCard
import com.udacity.astroapp.ui.components.MainTopAppBar
import com.udacity.astroapp.ui.components.SwipeableContent
import com.udacity.astroapp.ui.theme.AstroAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Destination
@Composable
fun AsteroidScreen(
    onNavigateToAsteroidDetails: (String) -> Unit = {},
    viewModel: AsteroidViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AsteroidSideEffect.ShowError -> {
                // Handle error display
            }
            is AsteroidSideEffect.NavigateToDetail ->
                onNavigateToAsteroidDetails(sideEffect.asteroid.asteroidId.toString())
            is AsteroidSideEffect.ShowDatePicker -> {}
        }
    }

    // Load initial data
    LaunchedEffect(Unit) { viewModel.loadAsteroids() }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = stringResource(R.string.screen_title_asteroids),
                selectedDate =
                    if (state.selectedDate.isNotBlank()) {
                        LocalDate.parse(state.selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    } else {
                        null
                    },
                onDateSelected = { selectedDate ->
                    val dateString = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    viewModel.handleAction(AsteroidAction.SelectDate(dateString))
                }
            )
        }
    ) { paddingValues ->
        SwipeableContent(
            currentDate =
                if (state.selectedDate.isNotBlank()) {
                    LocalDate.parse(state.selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
                } else {
                    LocalDate.now()
                },
            maxDate = null, // No max date restriction for asteroid screen
            onDateChanged = { newDate ->
                val dateString = newDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                viewModel.handleAction(AsteroidAction.SelectDate(dateString))
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            AsteroidScreenContent(
                state = state,
                onRetry = { viewModel.loadAsteroids() },
                onRefresh = { viewModel.loadAsteroids() },
                onNavigateToAsteroidDetails = onNavigateToAsteroidDetails,
                onHazardousFilterChange = { showHazardousOnly ->
                    viewModel.handleAction(AsteroidAction.FilterHazardous(showHazardousOnly))
                }
            )
        }
    }
}

@Composable
private fun AsteroidItem(asteroid: Asteroid, onClick: (Asteroid) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = { onClick(asteroid) }) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))) {
            Text(text = asteroid.asteroidName, style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date: ${asteroid.asteroidApproachDate}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (asteroid.asteroidIsHazardous) "⚠️ Hazardous" else "✅ Safe",
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (asteroid.asteroidIsHazardous) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_small)))

            Text(
                text =
                    "Diameter: ${asteroid.asteroidDiameterMin} - ${asteroid.asteroidDiameterMax} km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Velocity: ${asteroid.asteroidVelocity} km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AsteroidScreenContent(
    state: AsteroidState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToAsteroidDetails: (String) -> Unit,
    onHazardousFilterChange: (Boolean) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            pullToRefreshState.endRefresh()
        }
    }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        Column(modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.card_padding))) {
            // Display selected date
            if (state.selectedDate.isNotBlank()) {
                Text(
                    text = state.selectedDate,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(bottom = dimensionResource(R.dimen.spacing_medium))
                )
            }

            // Filter section
            AsteroidFilterCard(
                hazardousOnly = state.showHazardousOnly,
                onHazardousOnlyChange = onHazardousFilterChange
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.card_padding)))

            when {
                state.isLoading && state.filteredAsteroids.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))
                        ) {
                            Text(
                                text = state.error,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(
                                modifier = Modifier.height(dimensionResource(R.dimen.spacing_small))
                            )

                            Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                        }
                    }
                }
                state.filteredAsteroids.isNotEmpty() -> {
                    LazyColumn {
                        items(state.filteredAsteroids) { asteroid ->
                            AsteroidItem(
                                asteroid = asteroid,
                                onClick = {
                                    onNavigateToAsteroidDetails(asteroid.asteroidId.toString())
                                }
                            )
                            Spacer(
                                modifier = Modifier.height(dimensionResource(R.dimen.spacing_small))
                            )
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No asteroids found for the selected date range",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) { onRefresh() }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
private fun AsteroidScreenLoadingPreview() {
    AstroAppTheme {
        AsteroidScreenContent(
            state = AsteroidState(isLoading = true),
            onRetry = {},
            onRefresh = {},
            onNavigateToAsteroidDetails = {},
            onHazardousFilterChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AsteroidScreenErrorPreview() {
    AstroAppTheme {
        AsteroidScreenContent(
            state =
                AsteroidState(
                    isLoading = false,
                    error = "Failed to load asteroids. Please check your internet connection."
                ),
            onRetry = {},
            onRefresh = {},
            onNavigateToAsteroidDetails = {},
            onHazardousFilterChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AsteroidScreenEmptyPreview() {
    AstroAppTheme {
        AsteroidScreenContent(
            state =
                AsteroidState(
                    isLoading = false,
                    asteroids = emptyList(),
                    filteredAsteroids = emptyList(),
                    error = null
                ),
            onRetry = {},
            onRefresh = {},
            onNavigateToAsteroidDetails = {},
            onHazardousFilterChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AsteroidScreenHazardousFilterPreview() {
    AstroAppTheme {
        AsteroidScreenContent(
            state =
                AsteroidState(
                    isLoading = false,
                    showHazardousOnly = true,
                    asteroids =
                        listOf(
                            Asteroid(
                                asteroidId = 1,
                                asteroidName = "2024 AA1",
                                asteroidDiameterMin = 0.5,
                                asteroidDiameterMax = 1.2,
                                asteroidApproachDate = "2024-01-15",
                                asteroidVelocity = "25.8",
                                asteroidIsHazardous = true,
                                asteroidUrl = "https://example.com/asteroid1"
                            ),
                            Asteroid(
                                asteroidId = 2,
                                asteroidName = "2024 BB2",
                                asteroidDiameterMin = 2.1,
                                asteroidDiameterMax = 4.5,
                                asteroidApproachDate = "2024-01-16",
                                asteroidVelocity = "18.3",
                                asteroidIsHazardous = false,
                                asteroidUrl = "https://example.com/asteroid2"
                            )
                        ),
                    filteredAsteroids =
                        listOf(
                            Asteroid(
                                asteroidId = 1,
                                asteroidName = "2024 AA1",
                                asteroidDiameterMin = 0.5,
                                asteroidDiameterMax = 1.2,
                                asteroidApproachDate = "2024-01-15",
                                asteroidVelocity = "25.8",
                                asteroidIsHazardous = true,
                                asteroidUrl = "https://example.com/asteroid1"
                            )
                        ),
                    error = null
                ),
            onRetry = {},
            onRefresh = {},
            onNavigateToAsteroidDetails = {},
            onHazardousFilterChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AsteroidScreenSuccessPreview() {
    AstroAppTheme {
        AsteroidScreenContent(
            state =
                AsteroidState(
                    isLoading = false,
                    asteroids =
                        listOf(
                            Asteroid(
                                asteroidId = 1,
                                asteroidName = "2024 AA1",
                                asteroidDiameterMin = 0.5,
                                asteroidDiameterMax = 1.2,
                                asteroidApproachDate = "2024-01-15",
                                asteroidVelocity = "25.8",
                                asteroidIsHazardous = true,
                                asteroidUrl = "https://example.com/asteroid1"
                            ),
                            Asteroid(
                                asteroidId = 2,
                                asteroidName = "2024 BB2",
                                asteroidDiameterMin = 2.1,
                                asteroidDiameterMax = 4.5,
                                asteroidApproachDate = "2024-01-16",
                                asteroidVelocity = "18.3",
                                asteroidIsHazardous = false,
                                asteroidUrl = "https://example.com/asteroid2"
                            )
                        ),
                    filteredAsteroids =
                        listOf(
                            Asteroid(
                                asteroidId = 1,
                                asteroidName = "2024 AA1",
                                asteroidDiameterMin = 0.5,
                                asteroidDiameterMax = 1.2,
                                asteroidApproachDate = "2024-01-15",
                                asteroidVelocity = "25.8",
                                asteroidIsHazardous = true,
                                asteroidUrl = "https://example.com/asteroid1"
                            ),
                            Asteroid(
                                asteroidId = 2,
                                asteroidName = "2024 BB2",
                                asteroidDiameterMin = 2.1,
                                asteroidDiameterMax = 4.5,
                                asteroidApproachDate = "2024-01-16",
                                asteroidVelocity = "18.3",
                                asteroidIsHazardous = false,
                                asteroidUrl = "https://example.com/asteroid2"
                            )
                        ),
                    error = null
                ),
            onRetry = {},
            onRefresh = {},
            onNavigateToAsteroidDetails = {},
            onHazardousFilterChange = {}
        )
    }
}
