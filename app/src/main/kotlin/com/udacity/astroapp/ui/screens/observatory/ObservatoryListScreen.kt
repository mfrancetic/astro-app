package com.udacity.astroapp.ui.screens.observatory

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.ui.screens.destinations.ObservatoryDetailScreenDestination
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ObservatoryListScreen(
    navigator: DestinationsNavigator,
    viewModel: ObservatoryListViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION) { granted ->
        if (granted) {
            viewModel.onLocationPermissionGranted()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }
    
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Check if location is now enabled
        viewModel.loadObservatories(forceRefresh = true)
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ObservatoryListSideEffect.ShowError -> {
                // Handle error - could show snackbar
            }
            is ObservatoryListSideEffect.NavigateToObservatoryDetail -> {
                navigator.navigate(ObservatoryDetailScreenDestination(sideEffect.observatory.observatoryId ?: ""))
            }
            ObservatoryListSideEffect.RequestLocationPermission -> {
                locationPermissionState.launchPermissionRequest()
            }
            ObservatoryListSideEffect.OpenLocationSettings -> {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                settingsLauncher.launch(intent)
            }
            is ObservatoryListSideEffect.ShowSnackbar -> {
                // Handle snackbar
            }
        }
    }

    // Update permission state
    LaunchedEffect(locationPermissionState.status) {
        viewModel.onLocationPermissionGranted()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.menu_observatories),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            // Location status indicator
            if (!locationPermissionState.status.isGranted) {
                IconButton(onClick = { viewModel.requestLocationPermission() }) {
                    Icon(
                        Icons.Default.LocationOff,
                        contentDescription = stringResource(R.string.grant_location_permission),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.location_enabled),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Permission Banner
        if (!locationPermissionState.status.isGranted) {
            PermissionBanner(
                onGrantPermissionClick = { viewModel.requestLocationPermission() }
            )
        }

        // Content
        SwipeRefresh(
            state = rememberSwipeRefreshState(state.isRefreshing),
            onRefresh = { viewModel.onRefresh() }
        ) {
            when {
                state.isLoading -> {
                    LoadingContent()
                }
                state.error != null -> {
                    ErrorContent(
                        error = state.error!!,
                        onRetry = { viewModel.loadObservatories(forceRefresh = true) }
                    )
                }
                state.observatories.isEmpty() -> {
                    EmptyContent(
                        hasLocationPermission = locationPermissionState.status.isGranted,
                        onGrantPermission = { viewModel.requestLocationPermission() },
                        onOpenSettings = { viewModel.openLocationSettings() }
                    )
                }
                else -> {
                    ObservatoryList(
                        observatories = state.observatories,
                        onObservatoryClick = viewModel::onObservatoryClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionBanner(
    onGrantPermissionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.spacing_medium)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.location_permission_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.location_permission_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            TextButton(onClick = onGrantPermissionClick) {
                Text(stringResource(R.string.grant))
            }
        }
    }
    
    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            Text(
                text = stringResource(R.string.loading_observatories),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun EmptyContent(
    hasLocationPermission: Boolean,
    onGrantPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))
        ) {
            Icon(
                Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            
            Text(
                text = stringResource(R.string.no_observatories_found),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            
            Text(
                text = if (hasLocationPermission) {
                    stringResource(R.string.no_observatories_found_location_enabled)
                } else {
                    stringResource(R.string.no_observatories_found_no_location)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            
            if (!hasLocationPermission) {
                Button(onClick = onGrantPermission) {
                    Text(stringResource(R.string.grant_location_permission))
                }
            } else {
                Button(onClick = onOpenSettings) {
                    Text(stringResource(R.string.open_location_settings))
                }
            }
        }
    }
}

@Composable
private fun ObservatoryList(
    observatories: List<Observatory>,
    onObservatoryClick: (Observatory) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
    ) {
        items(observatories) { observatory ->
            ObservatoryItem(
                observatory = observatory,
                onClick = { onObservatoryClick(observatory) }
            )
        }
    }
}

@Composable
private fun ObservatoryItem(
    observatory: Observatory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.card_elevation)
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            Text(
                text = observatory.observatoryName ?: stringResource(R.string.unknown_observatory),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                
                Text(
                    text = observatory.observatoryAddress ?: stringResource(R.string.address_unknown),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Show open status if available
            if (observatory.observatoryOpenNow != null) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (observatory.observatoryOpenNow == true) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                    
                    Text(
                        text = if (observatory.observatoryOpenNow == true) {
                            stringResource(R.string.observatory_open)
                        } else {
                            stringResource(R.string.observatory_closed)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (observatory.observatoryOpenNow == true) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (observatory.observatoryOpenNow == true) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                    )
                }
            }
        }
    }
}