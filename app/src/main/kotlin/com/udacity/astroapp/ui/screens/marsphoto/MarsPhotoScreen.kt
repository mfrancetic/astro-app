package com.udacity.astroapp.ui.screens.marsphoto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Camera
import com.udacity.astroapp.models.MarsPhoto
import com.udacity.astroapp.models.Rover
import com.udacity.astroapp.ui.theme.AstroAppTheme
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

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MarsPhotoSideEffect.ShowError -> {
                // Handle error display
            }
            is MarsPhotoSideEffect.NavigateToDetail -> {}
            is MarsPhotoSideEffect.ShowDatePicker -> {}
        }
    }

    // Load initial data
    LaunchedEffect(Unit) { viewModel.loadPhotos() }

    MarsPhotoScreenContent(
        state = state,
        onRetry = { viewModel.loadPhotos() },
        onNavigateToFullScreen = onNavigateToFullScreen
    )
}

@Composable
private fun MarsPhotoScreenContent(
    state: MarsPhotoState,
    onRetry: () -> Unit,
    onNavigateToFullScreen: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.spacing_large))) {
        // Filter section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { /* Open date picker */}, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.select_date_button))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                ) {
                    Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))) {
                        Text(text = state.error, color = MaterialTheme.colorScheme.onErrorContainer)

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                    }
                }
            }
            state.marsPhotos.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement =
                        Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                    horizontalArrangement =
                        Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                ) {
                    items(state.marsPhotos) { marsPhoto ->
                        MarsPhotoItem(
                            marsPhoto = marsPhoto,
                            onClick = { onNavigateToFullScreen(marsPhoto.imageUrl) }
                        )
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Mars photos found for the selected filters",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(
                            modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium))
                        )

                        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarsPhotoItem(marsPhoto: MarsPhoto, onClick: (MarsPhoto) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().aspectRatio(1f), onClick = { onClick(marsPhoto) }) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = marsPhoto.imageUrl,
                contentDescription = "Mars photo from ${marsPhoto.rover?.roverName} rover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay with rover and camera info
            Surface(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_small))) {
                    Text(
                        text = marsPhoto.rover?.roverName ?: "Unknown Rover",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = marsPhoto.camera?.cameraName ?: "Unknown Camera",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
private fun MarsPhotoScreenLoadingPreview() {
    AstroAppTheme {
        MarsPhotoScreenContent(
            state = MarsPhotoState(isLoading = true),
            onRetry = {},
            onNavigateToFullScreen = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarsPhotoScreenErrorPreview() {
    AstroAppTheme {
        MarsPhotoScreenContent(
            state =
                MarsPhotoState(
                    isLoading = false,
                    error = "Failed to load Mars photos. Please check your internet connection."
                ),
            onRetry = {},
            onNavigateToFullScreen = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarsPhotoScreenEmptyPreview() {
    AstroAppTheme {
        MarsPhotoScreenContent(
            state = MarsPhotoState(isLoading = false, marsPhotos = emptyList(), error = null),
            onRetry = {},
            onNavigateToFullScreen = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarsPhotoScreenSuccessPreview() {
    AstroAppTheme {
        MarsPhotoScreenContent(
            state =
                MarsPhotoState(
                    isLoading = false,
                    marsPhotos =
                        listOf(
                            MarsPhoto(
                                id = 1,
                                sol = "2156",
                                imageUrl = "https://example.com/mars1.jpg",
                                earthDate = "2024-01-15",
                                camera =
                                    Camera(
                                        cameraName = "NAVCAM",
                                        cameraFullName = "Navigation Camera"
                                    ),
                                rover =
                                    Rover(
                                        roverName = "Curiosity",
                                        launchDate = "2011-11-26",
                                        landingDate = "2012-08-05"
                                    )
                            ),
                            MarsPhoto(
                                id = 2,
                                sol = "2157",
                                imageUrl = "https://example.com/mars2.jpg",
                                earthDate = "2024-01-16",
                                camera =
                                    Camera(cameraName = "MAST", cameraFullName = "Mast Camera"),
                                rover =
                                    Rover(
                                        roverName = "Perseverance",
                                        launchDate = "2020-07-30",
                                        landingDate = "2021-02-18"
                                    )
                            ),
                            MarsPhoto(
                                id = 3,
                                sol = "2158",
                                imageUrl = "https://example.com/mars3.jpg",
                                earthDate = "2024-01-17",
                                camera =
                                    Camera(
                                        cameraName = "FHAZ",
                                        cameraFullName = "Front Hazard Avoidance Camera"
                                    ),
                                rover =
                                    Rover(
                                        roverName = "Opportunity",
                                        launchDate = "2003-07-07",
                                        landingDate = "2004-01-25"
                                    )
                            ),
                            MarsPhoto(
                                id = 4,
                                sol = "2159",
                                imageUrl = "https://example.com/mars4.jpg",
                                earthDate = "2024-01-18",
                                camera =
                                    Camera(
                                        cameraName = "RHAZ",
                                        cameraFullName = "Rear Hazard Avoidance Camera"
                                    ),
                                rover =
                                    Rover(
                                        roverName = "Spirit",
                                        launchDate = "2003-06-10",
                                        landingDate = "2004-01-04"
                                    )
                            )
                        ),
                    error = null
                ),
            onRetry = {},
            onNavigateToFullScreen = {}
        )
    }
}
