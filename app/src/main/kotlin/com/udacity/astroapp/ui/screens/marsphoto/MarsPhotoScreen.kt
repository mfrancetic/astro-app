package com.udacity.astroapp.ui.screens.marsphoto

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
import com.udacity.astroapp.data.models.MarsPhoto
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Destination
@Composable
fun MarsPhotoScreen(
    onNavigateToFullScreen: (String) -> Unit = {},
    viewModel: MarsPhotoViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MarsPhotoSideEffect.ShowError -> {
                // Handle error display
            }
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.loadMarsPhotos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.spacing_large))
    ) {
        // Filter section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { /* Open rover picker */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.rover))
                    }

                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))

                    Button(
                        onClick = { /* Open camera picker */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.camera))
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { /* Open date picker */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.select_date_button))
                    }

                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))

                    Button(
                        onClick = { viewModel.loadMarsPhotos() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.refresh))
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
                            onClick = { viewModel.loadMarsPhotos() }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            state.marsPhotos.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                ) {
                    items(state.marsPhotos) { marsPhoto ->
                        MarsPhotoItem(
                            marsPhoto = marsPhoto,
                            onClick = { onNavigateToFullScreen(marsPhoto.imgSrc ?: "") }
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
                        text = "No Mars photos found for the selected filters",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun MarsPhotoItem(
    marsPhoto: MarsPhoto,
    onClick: (MarsPhoto) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = { onClick(marsPhoto) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = marsPhoto.imgSrc,
                contentDescription = "Mars photo from ${marsPhoto.rover?.name} rover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay with rover and camera info
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(R.dimen.spacing_small))
                ) {
                    Text(
                        text = marsPhoto.rover?.name ?: "Unknown Rover",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = marsPhoto.camera?.name ?: "Unknown Camera",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}