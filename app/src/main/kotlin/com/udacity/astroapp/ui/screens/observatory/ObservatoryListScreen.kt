package com.udacity.astroapp.ui.screens.observatory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ramcosta.composedestinations.annotation.Destination
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Observatory
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalPermissionsApi::class)
@Destination
@Composable
fun ObservatoryListScreen(
    onNavigateToObservatoryDetails: (String) -> Unit = {},
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
            is ObservatorySideEffect.ShowError -> {
                // Handle error display
            }
            is ObservatorySideEffect.NavigateToDetail -> onNavigateToObservatoryDetails(sideEffect.observatoryId.toString())
            is ObservatorySideEffect.CallPhone -> {}
            is ObservatorySideEffect.OpenWebsite -> {}
            is ObservatorySideEffect.NavigateBack -> {

            }
            is ObservatorySideEffect.NavigateToMaps -> {}
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        if (locationPermissionState.status.isGranted) {
            //TODO nearby observatories
            viewModel.loadObservatories()
        } else {
            viewModel.loadObservatories()
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
                                //TODO nearby observatories
                                viewModel.loadObservatories()
                            } else {
                                viewModel.loadObservatories()
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
                                    //TODO nearby observatories
                                    viewModel.loadObservatories()
                                } else {
                                    viewModel.loadObservatories()
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
                            onClick = { onNavigateToObservatoryDetails(observatory.observatoryId) }
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
                text = observatory.observatoryName,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

            Text(
                text = if (observatory.observatoryOpenNow) "Open" else "Closed",
                style = MaterialTheme.typography.bodySmall,
                color = if (observatory.observatoryOpenNow)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}