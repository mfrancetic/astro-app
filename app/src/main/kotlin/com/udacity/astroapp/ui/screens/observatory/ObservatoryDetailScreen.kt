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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.ramcosta.composedestinations.annotation.Destination
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.ui.theme.AstroAppTheme
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ObservatoryDetailScreen(
    observatoryId: Int,
    onNavigateBack: () -> Unit = {},
    viewModel: ObservatoryDetailViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ObservatorySideEffect.ShowError -> {
                // Handle error display
            }
            is ObservatorySideEffect.NavigateToDetail -> {}
            is ObservatorySideEffect.CallPhone -> {}
            is ObservatorySideEffect.OpenWebsite -> {}
            is ObservatorySideEffect.NavigateBack -> onNavigateBack()
            is ObservatorySideEffect.NavigateToMaps -> {}
        }
    }

    // Load observatory details
    LaunchedEffect(observatoryId) { viewModel.loadObservatory(observatoryId.toString()) }

    ObservatoryDetailScreenContent(
        isLoading = state.isLoading,
        observatory = state.observatory,
        error = state.error,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.loadObservatory(observatoryId.toString()) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ObservatoryDetailScreenContent(
    isLoading: Boolean,
    observatory: Observatory?,
    error: String?,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    observatory?.observatoryName
                        ?: stringResource(R.string.observatory_details_title)
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back_content_description)
                    )
                }
            }
        )

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Card(
                    modifier =
                        Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.spacing_large)),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                ) {
                    Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))) {
                        Text(text = error, color = MaterialTheme.colorScheme.onErrorContainer)

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                    }
                }
            }
            observatory != null -> {
                ObservatoryDetails(
                    observatory = observatory,
                    modifier =
                        Modifier.fillMaxSize().padding(dimensionResource(R.dimen.spacing_large))
                )
            }
        }
    }
}

@Composable
private fun ObservatoryDetails(observatory: Observatory, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        // Main info card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))) {
                Text(
                    text =
                        (observatory.observatoryName ?: "").ifEmpty {
                            stringResource(R.string.unknown_observatory)
                        },
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                // Address
                if ((observatory.observatoryAddress ?: "").isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        Text(
                            text = observatory.observatoryAddress ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                }

                // Opening status
                if ((observatory.observatoryOpeningHours ?: "").isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.observatory_status_label),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        Text(
                            text =
                                if (observatory.observatoryOpenNow)
                                    stringResource(R.string.observatory_open)
                                else stringResource(R.string.observatory_closed),
                            style = MaterialTheme.typography.bodyLarge,
                            color =
                                if (observatory.observatoryOpenNow)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

        // Google Maps integration
        if (observatory.observatoryLatitude != 0.0 && observatory.observatoryLongitude != 0.0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                ObservatoryMap(
                    latitude = observatory.observatoryLatitude,
                    longitude = observatory.observatoryLongitude,
                    observatoryName = observatory.observatoryName ?: "",
                    modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.map_height))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        }

        // Action buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    // Open maps app for directions
                    val geoUri =
                        "geo:${observatory.observatoryLatitude},${observatory.observatoryLongitude}?q=${observatory.observatoryLatitude},${observatory.observatoryLongitude}(${observatory.observatoryName})"
                    val mapIntent =
                        android.content.Intent(android.content.Intent.ACTION_VIEW, geoUri.toUri())
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                }
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                Text(stringResource(R.string.directions_button))
            }

            if ((observatory.observatoryPhoneNumber ?: "").isNotEmpty()) {
                Button(
                    onClick = {
                        val callIntent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                        callIntent.data = "tel:${observatory.observatoryPhoneNumber}".toUri()
                        context.startActivity(callIntent)
                    }
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                    Text(stringResource(R.string.call_button))
                }
            }
        }
    }
}

@Composable
private fun ObservatoryMap(
    latitude: Double,
    longitude: Double,
    observatoryName: String,
    modifier: Modifier = Modifier
) {
    val observatoryLocation = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(observatoryLocation, 15f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = true, mapToolbarEnabled = true)
    ) {
        Marker(
            state = MarkerState(position = observatoryLocation),
            title =
                stringResource(R.string.marker_of_the_observatory_content_description) +
                    " " +
                    observatoryName
        )
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
private fun ObservatoryDetailScreenLoadingPreview() {
    AstroAppTheme {
        ObservatoryDetailScreenContent(
            isLoading = true,
            observatory = null,
            error = null,
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ObservatoryDetailScreenErrorPreview() {
    AstroAppTheme {
        ObservatoryDetailScreenContent(
            isLoading = false,
            observatory = null,
            error = "Failed to load observatory details. Please check your internet connection.",
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ObservatoryDetailScreenSuccessPreview() {
    AstroAppTheme {
        ObservatoryDetailScreenContent(
            isLoading = false,
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
            error = null,
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ObservatoryDetailScreenClosedPreview() {
    AstroAppTheme {
        ObservatoryDetailScreenContent(
            isLoading = false,
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
            error = null,
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

// ObservatoryDetails Previews
@Preview(name = "Observatory Details Open - Light", showBackground = true)
@Composable
private fun ObservatoryDetailsOpenLightPreview() {
    AstroAppTheme(themePreference = 0) {
        ObservatoryDetails(
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
                )
        )
    }
}

@Preview(
    name = "Observatory Details Open - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ObservatoryDetailsOpenDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        ObservatoryDetails(
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
                )
        )
    }
}

@Preview(name = "Observatory Details Closed - Light", showBackground = true)
@Composable
private fun ObservatoryDetailsClosedLightPreview() {
    AstroAppTheme(themePreference = 0) {
        ObservatoryDetails(
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
                )
        )
    }
}

@Preview(
    name = "Observatory Details Closed - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ObservatoryDetailsClosedDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        ObservatoryDetails(
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
                )
        )
    }
}
