package com.udacity.astroapp.ui.screens.observatory

import android.content.res.Configuration
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
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ramcosta.composedestinations.annotation.Destination
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.ui.theme.AstroAppTheme
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
    // Location permission
    val locationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ObservatorySideEffect.ShowError -> {
                // Handle error display
            }
            is ObservatorySideEffect.NavigateToDetail ->
                onNavigateToObservatoryDetails(sideEffect.observatoryId)
            is ObservatorySideEffect.CallPhone -> {}
            is ObservatorySideEffect.OpenWebsite -> {}
            is ObservatorySideEffect.NavigateBack -> {}
            is ObservatorySideEffect.NavigateToMaps -> {}
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        if (locationPermissionState.status.isGranted) {
            // TODO nearby observatories
            viewModel.loadObservatories()
        } else {
            viewModel.loadObservatories()
        }
    }

    ObservatoryListScreenContent(
        isLoading = state.isLoading,
        observatories = state.observatories,
        error = state.error,
        hasLocationPermission = locationPermissionState.status.isGranted,
        onRequestLocationPermission = { locationPermissionState.launchPermissionRequest() },
        onLoadObservatories = { viewModel.loadObservatories() },
        onRefresh = { viewModel.loadObservatories() },
        onNavigateToObservatoryDetails = onNavigateToObservatoryDetails
    )
}

@Composable
private fun ObservatoryListScreenContent(
    isLoading: Boolean,
    observatories: List<Observatory>,
    error: String?,
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onLoadObservatories: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToObservatoryDetails: (String) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            pullToRefreshState.endRefresh()
        }
    }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.spacing_large))
        ) {
            // Location permission and search section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))) {
                    if (!hasLocationPermission) {
                        Text(
                            text = "Location permission needed for nearby observatories",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                        Button(onClick = onRequestLocationPermission) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(
                                modifier = Modifier.width(dimensionResource(R.dimen.spacing_small))
                            )
                            Text(stringResource(R.string.grant_location_permission_button))
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = onLoadObservatories, modifier = Modifier.weight(1f)) {
                            Text(if (hasLocationPermission) "Find Nearby" else "Show All")
                        }

                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))

                        Button(
                            onClick = { /* Open search dialog */},
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.search))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            when {
                isLoading && observatories.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))
                        ) {
                            Text(text = error, color = MaterialTheme.colorScheme.onErrorContainer)

                            Spacer(
                                modifier = Modifier.height(dimensionResource(R.dimen.spacing_small))
                            )

                            Button(onClick = onLoadObservatories) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
                observatories.isNotEmpty() -> {
                    LazyColumn {
                        items(observatories, key = { it.observatoryId }) { observatory ->
                            ObservatoryItem(
                                observatory = observatory,
                                onClick = {
                                    onNavigateToObservatoryDetails(observatory.observatoryId)
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
                            text = "No observatories found",
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

@Composable
private fun ObservatoryItem(observatory: Observatory, onClick: (Observatory) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = { onClick(observatory) }) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))) {
            Text(
                text = observatory.observatoryName ?: "",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

            Text(
                text = if (observatory.observatoryOpenNow) "Open" else "Closed",
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (observatory.observatoryOpenNow) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
            )
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
private fun ObservatoryListScreenLoadingPreview() {
    AstroAppTheme {
        ObservatoryListScreenContent(
            isLoading = true,
            observatories = emptyList(),
            error = null,
            hasLocationPermission = false,
            onRequestLocationPermission = {},
            onLoadObservatories = {},
            onRefresh = {},
            onNavigateToObservatoryDetails = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ObservatoryListScreenErrorPreview() {
    AstroAppTheme {
        ObservatoryListScreenContent(
            isLoading = false,
            observatories = emptyList(),
            error = "Failed to load observatories. Please check your internet connection.",
            hasLocationPermission = true,
            onRequestLocationPermission = {},
            onLoadObservatories = {},
            onRefresh = {},
            onNavigateToObservatoryDetails = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ObservatoryListScreenEmptyPreview() {
    AstroAppTheme {
        ObservatoryListScreenContent(
            isLoading = false,
            observatories = emptyList(),
            error = null,
            hasLocationPermission = false,
            onRequestLocationPermission = {},
            onLoadObservatories = {},
            onRefresh = {},
            onNavigateToObservatoryDetails = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ObservatoryListScreenSuccessPreview() {
    AstroAppTheme {
        ObservatoryListScreenContent(
            isLoading = false,
            observatories =
                listOf(
                    Observatory(
                        observatoryId = "1",
                        observatoryName = "Griffith Observatory",
                        observatoryAddress = "2800 E Observatory Rd, Los Angeles, CA 90027",
                        observatoryPhoneNumber = "+1 (213) 473-0800",
                        observatoryOpenNow = true,
                        observatoryOpeningHours = "Tue-Fri 12pm-10pm, Sat-Sun 10am-10pm",
                        observatoryLatitude = 34.1184,
                        observatoryLongitude = -118.3004,
                        observatoryUrl = "https://griffithobservatory.org/"
                    ),
                    Observatory(
                        observatoryId = "2",
                        observatoryName = "Mount Wilson Observatory",
                        observatoryAddress = "Mt Wilson Observatory Rd, Mt Wilson, CA 91023",
                        observatoryPhoneNumber = "+1 (626) 440-9016",
                        observatoryOpenNow = false,
                        observatoryOpeningHours = "Weekends 10am-5pm (Apr-Nov)",
                        observatoryLatitude = 34.2259,
                        observatoryLongitude = -118.0572,
                        observatoryUrl = "https://www.mtwilson.edu/"
                    ),
                    Observatory(
                        observatoryId = "3",
                        observatoryName = "Palomar Observatory",
                        observatoryAddress = "35899 Canfield Rd, Palomar Mountain, CA 92060",
                        observatoryPhoneNumber = "+1 (760) 742-2119",
                        observatoryOpenNow = true,
                        observatoryOpeningHours = "Daily 9am-4pm",
                        observatoryLatitude = 33.3563,
                        observatoryLongitude = -116.8650,
                        observatoryUrl = "https://www.astro.caltech.edu/palomar/"
                    )
                ),
            error = null,
            hasLocationPermission = true,
            onRequestLocationPermission = {},
            onLoadObservatories = {},
            onRefresh = {},
            onNavigateToObservatoryDetails = {}
        )
    }
}

// ObservatoryItem Previews
@Preview(name = "Observatory Item Open - Light", showBackground = true)
@Composable
private fun ObservatoryItemOpenLightPreview() {
    AstroAppTheme(themePreference = 0) {
        ObservatoryItem(
            observatory =
                Observatory(
                    observatoryId = "1",
                    observatoryName = "Griffith Observatory",
                    observatoryAddress = "2800 E Observatory Rd, Los Angeles, CA 90027",
                    observatoryPhoneNumber = "+1 (213) 473-0800",
                    observatoryOpenNow = true,
                    observatoryOpeningHours = "Tue-Fri 12pm-10pm, Sat-Sun 10am-10pm",
                    observatoryLatitude = 34.1184,
                    observatoryLongitude = -118.3004,
                    observatoryUrl = "https://griffithobservatory.org/"
                ),
            onClick = {}
        )
    }
}

@Preview(
    name = "Observatory Item Open - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ObservatoryItemOpenDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        ObservatoryItem(
            observatory =
                Observatory(
                    observatoryId = "1",
                    observatoryName = "Griffith Observatory",
                    observatoryAddress = "2800 E Observatory Rd, Los Angeles, CA 90027",
                    observatoryPhoneNumber = "+1 (213) 473-0800",
                    observatoryOpenNow = true,
                    observatoryOpeningHours = "Tue-Fri 12pm-10pm, Sat-Sun 10am-10pm",
                    observatoryLatitude = 34.1184,
                    observatoryLongitude = -118.3004,
                    observatoryUrl = "https://griffithobservatory.org/"
                ),
            onClick = {}
        )
    }
}

@Preview(name = "Observatory Item Closed - Light", showBackground = true)
@Composable
private fun ObservatoryItemClosedLightPreview() {
    AstroAppTheme(themePreference = 0) {
        ObservatoryItem(
            observatory =
                Observatory(
                    observatoryId = "2",
                    observatoryName = "Mount Wilson Observatory",
                    observatoryAddress = "Mt Wilson Observatory Rd, Mt Wilson, CA 91023",
                    observatoryPhoneNumber = "+1 (626) 440-9016",
                    observatoryOpenNow = false,
                    observatoryOpeningHours = "Weekends 10am-5pm (Apr-Nov)",
                    observatoryLatitude = 34.2259,
                    observatoryLongitude = -118.0572,
                    observatoryUrl = "https://www.mtwilson.edu/"
                ),
            onClick = {}
        )
    }
}

@Preview(
    name = "Observatory Item Closed - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ObservatoryItemClosedDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        ObservatoryItem(
            observatory =
                Observatory(
                    observatoryId = "2",
                    observatoryName = "Mount Wilson Observatory",
                    observatoryAddress = "Mt Wilson Observatory Rd, Mt Wilson, CA 91023",
                    observatoryPhoneNumber = "+1 (626) 440-9016",
                    observatoryOpenNow = false,
                    observatoryOpeningHours = "Weekends 10am-5pm (Apr-Nov)",
                    observatoryLatitude = 34.2259,
                    observatoryLongitude = -118.0572,
                    observatoryUrl = "https://www.mtwilson.edu/"
                ),
            onClick = {}
        )
    }
}
