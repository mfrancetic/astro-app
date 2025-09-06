package com.udacity.astroapp.ui.screens.asteroid

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.ramcosta.composedestinations.annotation.Destination
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.ui.components.SearchTextField
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showFilterDialog by remember { mutableStateOf(false) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AsteroidSideEffect.ShowError -> {
                // Error handled via state
            }

            is AsteroidSideEffect.OpenAsteroidUrl -> {
                val intent = Intent(Intent.ACTION_VIEW, sideEffect.url.toUri())
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Could trigger error state in ViewModel
                }
            }
        }
    }


    // Handle error state from ViewModel
    state.error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
            SearchTextField(
                initialValue = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.search_asteroids),
                searchContentDescription = stringResource(R.string.cd_search),
                clearContentDescription = stringResource(R.string.cd_clear)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (state.showHazardousOnly) Icons.Default.Warning else Icons.Default.Dangerous,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                            tint = if (state.showHazardousOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column {
                            Text(
                                text = if (state.showHazardousOnly) "Hazardous only" else "All asteroids",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (state.showHazardousOnly) "Showing dangerous asteroids only" else "Showing all near-Earth asteroids",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = state.showHazardousOnly,
                        onCheckedChange = { viewModel.onHazardousFilterToggled() }
                    )
                }
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
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            sortOption = state.sortBy,
            onSortChanged = {
                viewModel.onSortOptionChanged(it)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun AsteroidCard(
    asteroid: Asteroid,
    onUrlClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                text = "Diameter: ${
                    String.format(
                        "%.2f",
                        asteroid.asteroidDiameterMin
                    )
                } - ${String.format("%.2f", asteroid.asteroidDiameterMax)} km",
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
    sortOption: AsteroidSortOption,
    onSortChanged: (AsteroidSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSortOption by rememberSaveable { mutableStateOf(sortOption) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Asteroids") },
        text = {
            Column {
                AsteroidSortOption.entries.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                currentSortOption = option
                            }
                    ) {
                        RadioButton(
                            selected = currentSortOption == option,
                            onClick = {
                                currentSortOption = option
                            }
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
            TextButton(onClick = {
                onSortChanged(currentSortOption)
            }) {
                Text("Done")
            }
        }
    )
}