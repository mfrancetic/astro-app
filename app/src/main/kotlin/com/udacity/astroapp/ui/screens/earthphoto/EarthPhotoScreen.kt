package com.udacity.astroapp.ui.screens.earthphoto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import coil.compose.AsyncImage
import com.udacity.astroapp.R
import com.udacity.astroapp.data.models.EarthPhoto
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Destination
@Composable
fun EarthPhotoScreen(
    onNavigateToFullScreen: (String) -> Unit = {},
    viewModel: EarthPhotoViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is EarthPhotoSideEffect.ShowError -> {
                // Handle error display
            }
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.loadEarthPhotos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.spacing_large))
    ) {
        // Date selection section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.spacing_large)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* Open date picker */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.select_date_button))
                }

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))

                Button(
                    onClick = { viewModel.loadEarthPhotos() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.refresh))
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
                            onClick = { viewModel.loadEarthPhotos() }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            state.earthPhotos.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                ) {
                    items(state.earthPhotos) { earthPhoto ->
                        EarthPhotoItem(
                            earthPhoto = earthPhoto,
                            onClick = { onNavigateToFullScreen(earthPhoto.image ?: "") }
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Earth photos found for the selected date",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun EarthPhotoItem(
    earthPhoto: EarthPhoto,
    onClick: (EarthPhoto) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = { onClick(earthPhoto) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = earthPhoto.image,
                contentDescription = "Earth photo from ${earthPhoto.date}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay with date
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Text(
                    text = earthPhoto.date ?: "Unknown Date",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(dimensionResource(R.dimen.spacing_small))
                )
            }
        }
    }
}