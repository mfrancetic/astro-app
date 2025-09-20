package com.udacity.astroapp.ui.screens.earthphoto

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
import androidx.compose.foundation.layout.width
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
import com.udacity.astroapp.models.EarthPhoto
import com.udacity.astroapp.ui.theme.AstroAppTheme
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

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is EarthPhotoSideEffect.ShowError -> {
                // Handle error display
            }
            is EarthPhotoSideEffect.NavigateToDetail -> {}
            is EarthPhotoSideEffect.ShowDatePicker -> {}
        }
    }

    // Load initial data
    LaunchedEffect(Unit) { viewModel.loadPhotos() }

    EarthPhotoScreenContent(
        state = state,
        onRetry = { viewModel.loadPhotos() },
        onNavigateToFullScreen = onNavigateToFullScreen
    )
}

@Composable
private fun EarthPhotoScreenContent(
    state: EarthPhotoState,
    onRetry: () -> Unit,
    onNavigateToFullScreen: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.spacing_large))) {
        // Date selection section
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier =
                    Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.spacing_large)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { /* Open date picker */}, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.select_date_button))
                }

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
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
                    }
                }
            }
            state.earthPhotos.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement =
                        Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                    horizontalArrangement =
                        Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                ) {
                    items(state.earthPhotos) { earthPhoto ->
                        EarthPhotoItem(
                            earthPhoto = earthPhoto,
                            onClick = { onNavigateToFullScreen(earthPhoto.earthPhotoUrl) }
                        )
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Earth photos found for the selected date",
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
private fun EarthPhotoItem(earthPhoto: EarthPhoto, onClick: (EarthPhoto) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().aspectRatio(1f), onClick = { onClick(earthPhoto) }) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = earthPhoto.earthPhotoUrl,
                contentDescription = "Earth photo from ${earthPhoto.earthPhotoDateTime}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay with date
            Surface(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Text(
                    text = earthPhoto.earthPhotoDateTime,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(dimensionResource(R.dimen.spacing_small))
                )
            }
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
private fun EarthPhotoScreenLoadingPreview() {
    AstroAppTheme {
        EarthPhotoScreenContent(
            state = EarthPhotoState(isLoading = true),
            onRetry = {},
            onNavigateToFullScreen = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EarthPhotoScreenErrorPreview() {
    AstroAppTheme {
        EarthPhotoScreenContent(
            state =
                EarthPhotoState(
                    isLoading = false,
                    error = "Failed to load Earth photos. Please check your internet connection."
                ),
            onRetry = {},
            onNavigateToFullScreen = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EarthPhotoScreenEmptyPreview() {
    AstroAppTheme {
        EarthPhotoScreenContent(
            state = EarthPhotoState(isLoading = false, earthPhotos = emptyList(), error = null),
            onRetry = {},
            onNavigateToFullScreen = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EarthPhotoScreenSuccessPreview() {
    AstroAppTheme {
        EarthPhotoScreenContent(
            state =
                EarthPhotoState(
                    isLoading = false,
                    earthPhotos =
                        listOf(
                            EarthPhoto(
                                earthPhotoId = 1,
                                earthPhotoUrl = "https://example.com/earth1.jpg",
                                earthPhotoDateTime = "2024-01-15 12:30:45"
                            ),
                            EarthPhoto(
                                earthPhotoId = 2,
                                earthPhotoUrl = "https://example.com/earth2.jpg",
                                earthPhotoDateTime = "2024-01-15 13:45:20"
                            ),
                            EarthPhoto(
                                earthPhotoId = 3,
                                earthPhotoUrl = "https://example.com/earth3.jpg",
                                earthPhotoDateTime = "2024-01-15 15:12:10"
                            ),
                            EarthPhoto(
                                earthPhotoId = 4,
                                earthPhotoUrl = "https://example.com/earth4.jpg",
                                earthPhotoDateTime = "2024-01-15 16:28:33"
                            )
                        ),
                    error = null
                ),
            onRetry = {},
            onNavigateToFullScreen = {}
        )
    }
}
