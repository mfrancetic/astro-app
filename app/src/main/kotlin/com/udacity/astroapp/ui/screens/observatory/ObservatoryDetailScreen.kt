package com.udacity.astroapp.ui.screens.observatory

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Observatory
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ObservatoryDetailScreen(
    observatoryId: String,
    navigator: DestinationsNavigator,
    viewModel: ObservatoryDetailViewModel = koinViewModel { parametersOf(observatoryId) }
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ObservatoryDetailSideEffect.ShowError -> {
                // Handle error - could show snackbar
            }
            is ObservatoryDetailSideEffect.OpenWebsite -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sideEffect.url))
                context.startActivity(intent)
            }
            is ObservatoryDetailSideEffect.CallPhoneNumber -> {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${sideEffect.phoneNumber}"))
                context.startActivity(intent)
            }
            is ObservatoryDetailSideEffect.OpenInMaps -> {
                val uri = Uri.parse("geo:${sideEffect.latitude},${sideEffect.longitude}?q=${sideEffect.latitude},${sideEffect.longitude}(${sideEffect.title})")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
            is ObservatoryDetailSideEffect.ShowSnackbar -> {
                // Handle snackbar
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Loading State
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        // Error State
        state.error?.let { error ->
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
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                    Button(onClick = { viewModel.refresh() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
            return
        }

        // Observatory Content
        state.observatory?.let { observatory ->
            ObservatoryContent(
                observatory = observatory,
                onWebsiteClick = viewModel::onWebsiteClicked,
                onPhoneClick = viewModel::onPhoneClicked,
                onMapClick = viewModel::onMapClicked,
                onMapReady = viewModel::onMapReady,
                isMapReady = state.isMapReady
            )
        }
    }
}

@Composable
private fun ObservatoryContent(
    observatory: Observatory,
    onWebsiteClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onMapClick: () -> Unit,
    onMapReady: () -> Unit,
    isMapReady: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            Text(
                text = observatory.observatoryName ?: stringResource(R.string.unknown_observatory),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                
                Text(
                    text = observatory.observatoryAddress ?: stringResource(R.string.address_unknown),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Status and Info Cards
        Column(
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.spacing_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            // Open Status
            observatory.observatoryOpenNow?.let { isOpen ->
                StatusCard(
                    isOpen = isOpen,
                    openingHours = observatory.observatoryOpeningHours
                )
            }
            
            // Contact Information
            if (!observatory.observatoryPhoneNumber.isNullOrBlank() || 
                !observatory.observatoryUrl.isNullOrBlank()) {
                ContactCard(
                    phoneNumber = observatory.observatoryPhoneNumber,
                    website = observatory.observatoryUrl,
                    onPhoneClick = onPhoneClick,
                    onWebsiteClick = onWebsiteClick
                )
            }
        }
        
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        
        // Map Section
        MapSection(
            observatory = observatory,
            onMapClick = onMapClick,
            onMapReady = onMapReady,
            isMapReady = isMapReady
        )
    }
}

@Composable
private fun StatusCard(
    isOpen: Boolean,
    openingHours: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        colors = CardDefaults.cardColors(
            containerColor = if (isOpen) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isOpen) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                
                Text(
                    text = if (isOpen) {
                        stringResource(R.string.observatory_open)
                    } else {
                        stringResource(R.string.observatory_closed)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOpen) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            openingHours?.let { hours ->
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                Text(
                    text = hours,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ContactCard(
    phoneNumber: String?,
    website: String?,
    onPhoneClick: () -> Unit,
    onWebsiteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius))
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            Text(
                text = stringResource(R.string.contact_information),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            
            // Phone Number
            phoneNumber?.takeIf { it.isNotBlank() }?.let { phone ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                    
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    TextButton(onClick = onPhoneClick) {
                        Text(stringResource(R.string.call))
                    }
                }
            }
            
            // Website
            website?.takeIf { it.isNotBlank() }?.let { url ->
                if (!phoneNumber.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                    
                    Text(
                        text = stringResource(R.string.website),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    TextButton(onClick = onWebsiteClick) {
                        Text(stringResource(R.string.visit))
                    }
                }
            }
        }
    }
}

@Composable
private fun MapSection(
    observatory: Observatory,
    onMapClick: () -> Unit,
    onMapReady: () -> Unit,
    isMapReady: Boolean
) {
    Column {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.spacing_medium)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.location),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            TextButton(onClick = onMapClick) {
                Text(stringResource(R.string.open_in_maps))
            }
        }
        
        // Map
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = dimensionResource(R.dimen.spacing_medium)),
            shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius))
        ) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        onCreate(null)
                        getMapAsync { googleMap ->
                            val observatoryLocation = LatLng(
                                observatory.observatoryLatitude,
                                observatory.observatoryLongitude
                            )
                            
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(observatoryLocation)
                                    .title(observatory.observatoryName ?: "Observatory")
                            )
                            
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(observatoryLocation, 15f)
                            )
                            
                            onMapReady()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { mapView ->
                mapView.onResume()
            }
        }
        
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
    }
}