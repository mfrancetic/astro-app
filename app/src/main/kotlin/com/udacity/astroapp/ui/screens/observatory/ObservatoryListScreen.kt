package com.udacity.astroapp.ui.screens.observatory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.udacity.astroapp.R
import com.udacity.astroapp.data.models.Observatory
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalPermissionsApi::class)
@Destination
@Composable
fun ObservatoryListScreen(
    onNavigateToObservatoryDetails: (Int) -> Unit = {},
    viewModel: ObservatoryViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    // Location permission
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ObservatoryListSideEffect.ShowError -> {
                // Handle error display
            }
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        if (locationPermissionState.status.isGranted) {
            viewModel.loadNearbyObservatories()
        } else {
            viewModel.loadAllObservatories()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.spacing_large))
    ) {
        // Location permission and search section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))
            ) {
                if (!locationPermissionState.status.isGranted) {
                    Text(
                        text = "Location permission needed for nearby observatories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                    Button(
                        onClick = { locationPermissionState.launchPermissionRequest() }
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        Text(stringResource(R.string.grant_location_permission_button))
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            if (locationPermissionState.status.isGranted) {
                                viewModel.loadNearbyObservatories()
                            } else {
                                viewModel.loadAllObservatories()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (locationPermissionState.status.isGranted) "Find Nearby" else "Show All")
                    }

                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))

                    Button(
                        onClick = { /* Open search dialog */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.search))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

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
                        modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))
                    ) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                        Button(
                            onClick = {
                                if (locationPermissionState.status.isGranted) {
                                    viewModel.loadNearbyObservatories()
                                } else {
                                    viewModel.loadAllObservatories()
                                }
                            }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            state.observatories.isNotEmpty() -> {
                LazyColumn {
                    items(state.observatories) { observatory ->
                        ObservatoryItem(
                            observatory = observatory,
                            onClick = { onNavigateToObservatoryDetails(observatory.id) }
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
                        text = "No observatories found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ObservatoryItem(
    observatory: Observatory,
    onClick: (Observatory) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = { onClick(observatory) }
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))
        ) {
            Text(
                text = observatory.name ?: "Unknown Observatory",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

            if (observatory.vicinity != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_small)),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_extra_small)))
                    Text(
                        text = observatory.vicinity!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_small)))
            }

            if (observatory.rating != null) {
                Text(
                    text = "Rating: ${observatory.rating}/5.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (observatory.openingHours != null) {
                Text(
                    text = if (observatory.openingHours!!) "Open" else "Closed",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (observatory.openingHours!!)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}