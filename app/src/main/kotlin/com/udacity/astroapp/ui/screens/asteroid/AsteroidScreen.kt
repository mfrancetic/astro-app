package com.udacity.astroapp.ui.screens.asteroid

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DangerousOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Asteroid
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Destination
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsteroidScreen(
    viewModel: AsteroidViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AsteroidSideEffect.ShowError -> {
                // Handle error
            }
            is AsteroidSideEffect.ShowAsteroidDetails -> {
                // Handle asteroid details
            }
            is AsteroidSideEffect.OpenAsteroidUrl -> {
                // Handle URL opening
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.spacing_medium))
    ) {
        // Title and Filter Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.title_near_earth_asteroids),
                style = MaterialTheme.typography.headlineSmall
            )
            
            IconButton(onClick = { showFilterDialog = true }) {
                Icon(
                    Icons.Default.FilterList, 
                    contentDescription = stringResource(R.string.cd_filter)
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.search_asteroids)) },
            leadingIcon = { 
                Icon(
                    Icons.Default.Search, 
                    contentDescription = stringResource(R.string.cd_search)
                ) 
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.showHazardousOnly,
                onClick = { viewModel.onHazardousFilterToggled() },
                label = { Text("Hazardous Only") },
                leadingIcon = {
                    Icon(
                        if (state.showHazardousOnly) Icons.Default.Warning else Icons.Default.DangerousOff,
                        contentDescription = "Hazardous filter"
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading State
        if (state.isLoading && state.asteroids.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Asteroid List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(state.filteredAsteroids) { asteroid ->
                AsteroidCard(
                    asteroid = asteroid,
                    onAsteroidClick = { viewModel.onAsteroidClicked(asteroid) },
                    onUrlClick = { viewModel.onAsteroidUrlClicked(asteroid.asteroidUrl) }
                )
            }
        }

        // Error State
        state.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            currentSort = state.sortBy,
            onSortChanged = { viewModel.onSortOptionChanged(it) },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun AsteroidCard(
    asteroid: Asteroid,
    onAsteroidClick: () -> Unit,
    onUrlClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onAsteroidClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = asteroid.asteroidName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                if (asteroid.asteroidIsHazardous) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Hazardous",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Approach Date: ${asteroid.asteroidApproachDate}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Diameter: ${String.format("%.2f", asteroid.asteroidDiameterMin)} - ${String.format("%.2f", asteroid.asteroidDiameterMax)} km",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Velocity: ${asteroid.asteroidVelocity}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onUrlClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("More Details")
            }
        }
    }
}

@Composable
fun FilterDialog(
    currentSort: AsteroidSortOption,
    onSortChanged: (AsteroidSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Asteroids") },
        text = {
            Column {
                AsteroidSortOption.values().forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = currentSort == option,
                            onClick = { onSortChanged(option) }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = when (option) {
                                AsteroidSortOption.DATE -> "Date"
                                AsteroidSortOption.NAME -> "Name"
                                AsteroidSortOption.SIZE -> "Size"
                                AsteroidSortOption.VELOCITY -> "Velocity"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}