package com.udacity.astroapp.ui.screens.asteroid

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.dimensionResource
import com.ramcosta.composedestinations.annotation.Destination
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Asteroid
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
    val context = LocalContext.current

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AsteroidSideEffect.ShowError -> {
                // Handle error display
            }
            is AsteroidSideEffect.NavigateToDetail -> onNavigateToAsteroidDetails(sideEffect.asteroid.asteroidId.toString())
            is AsteroidSideEffect.ShowDatePicker -> {

            }
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.loadAsteroids()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.card_padding))
    ) {
        // Search and filter section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.card_padding)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* Open date picker */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.select_date_range))
                }

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))

                Button(
                    onClick = { viewModel.loadAsteroids() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.refresh))
                }
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.card_padding)))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))
                    ) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                        Button(
                            onClick = { viewModel.loadAsteroids() }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            state.asteroids.isNotEmpty() -> {
                LazyColumn {
                    items(state.asteroids) { asteroid ->
                        AsteroidItem(
                            asteroid = asteroid,
                            onClick = { onNavigateToAsteroidDetails(asteroid.asteroidId.toString()) }
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No asteroids found for the selected date range",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun AsteroidItem(
    asteroid: Asteroid,
    onClick: (Asteroid) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = { onClick(asteroid) }
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))
        ) {
            Text(
                text = asteroid.asteroidName ?: "Unknown Asteroid",
                style = MaterialTheme.typography.headlineSmall
            )

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
                    color = if (asteroid.asteroidIsHazardous)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_small)))

            Text(
                text = "Diameter: ${asteroid.asteroidDiameterMin} - ${asteroid.asteroidDiameterMax} km",
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