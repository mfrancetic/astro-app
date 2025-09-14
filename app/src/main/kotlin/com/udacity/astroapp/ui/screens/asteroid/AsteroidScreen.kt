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
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.udacity.astroapp.R
import com.udacity.astroapp.data.models.Asteroid
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
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.loadAsteroids()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search and filter section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* Open date picker */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Select Date Range")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.loadAsteroids() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Refresh")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
                            onClick = { onNavigateToAsteroidDetails(asteroid.id.toString()) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = asteroid.name ?: "Unknown Asteroid",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date: ${asteroid.closeApproachDate}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (asteroid.isPotentiallyHazardousAsteroid) "⚠️ Hazardous" else "✅ Safe",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (asteroid.isPotentiallyHazardousAsteroid)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Diameter: ${asteroid.estimatedDiameterMin} - ${asteroid.estimatedDiameterMax} km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Distance: ${asteroid.missDistanceKilometers} km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}