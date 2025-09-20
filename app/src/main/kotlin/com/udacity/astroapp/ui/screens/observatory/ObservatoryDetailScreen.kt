package com.udacity.astroapp.ui.screens.observatory

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.ramcosta.composedestinations.annotation.Destination
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Observatory
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
    val context = LocalContext.current

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
    LaunchedEffect(observatoryId) {
        viewModel.loadObservatory(observatoryId.toString())
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar
        TopAppBar(
            title = { Text(state.observatory?.observatoryName ?: stringResource(R.string.observatory_details_title)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_content_description))
                }
            }
        )

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.spacing_large)),
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
                            onClick = { viewModel.loadObservatory(observatoryId.toString()) }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            state.observatory != null -> {
                ObservatoryDetails(
                    observatory = state.observatory!!,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.spacing_large))
                )
            }
        }
    }
}

@Composable
private fun ObservatoryDetails(
    observatory: Observatory,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
    ) {
        // Main info card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))
            ) {
                Text(
                    text = observatory.observatoryName.ifEmpty { stringResource(R.string.unknown_observatory) },
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                // Address
                if (observatory.observatoryAddress.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        Text(
                            text = observatory.observatoryAddress,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                }

                // Opening status
                if (observatory.observatoryOpeningHours.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.observatory_status_label),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        Text(
                            text = if (observatory.observatoryOpenNow)
                                stringResource(R.string.observatory_open)
                            else
                                stringResource(R.string.observatory_closed),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (observatory.observatoryOpenNow)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

        // Google Maps integration
        if (observatory.observatoryLatitude != 0.0 && observatory.observatoryLongitude != 0.0) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                ObservatoryMap(
                    latitude = observatory.observatoryLatitude,
                    longitude = observatory.observatoryLongitude,
                    observatoryName = observatory.observatoryName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.map_height))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    // Open maps app for directions
                    val geoUri = "geo:${observatory.observatoryLatitude},${observatory.observatoryLongitude}?q=${observatory.observatoryLatitude},${observatory.observatoryLongitude}(${observatory.observatoryName})"
                    val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(geoUri))
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                }
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                Text(stringResource(R.string.directions_button))
            }

            if (observatory.observatoryPhoneNumber.isNotEmpty()) {
                Button(
                    onClick = {
                        val callIntent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                        callIntent.data = android.net.Uri.parse("tel:${observatory.observatoryPhoneNumber}")
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
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            mapToolbarEnabled = true
        )
    ) {
        Marker(
            state = MarkerState(position = observatoryLocation),
            title = stringResource(R.string.marker_of_the_observatory_content_description) + " " + observatoryName
        )
    }
}