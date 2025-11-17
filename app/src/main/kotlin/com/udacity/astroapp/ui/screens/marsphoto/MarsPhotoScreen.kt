package com.udacity.astroapp.ui.screens.marsphoto

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Camera
import com.udacity.astroapp.models.MarsPhoto
import com.udacity.astroapp.models.Rover
import com.udacity.astroapp.ui.components.FullScreenPhotoDialog
import com.udacity.astroapp.ui.components.MainTopAppBar
import com.udacity.astroapp.ui.components.SwipeableContent
import com.udacity.astroapp.ui.theme.AstroAppTheme
import com.udacity.astroapp.utils.PhotoSharingUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

// Utility functions for LocalDate <-> String conversion
private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

private fun LocalDate.toDateString(): String = this.format(dateFormatter)

private fun String.toLocalDate(): LocalDate = LocalDate.parse(this, dateFormatter)

@Destination
@Composable
fun MarsPhotoScreen(viewModel: MarsPhotoViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    var showFullScreenPhoto by remember { mutableStateOf(false) }
    var selectedMarsPhoto by remember { mutableStateOf<MarsPhoto?>(null) }

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MarsPhotoSideEffect.ShowError -> {
                // Handle error display
            }
        }
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = stringResource(R.string.screen_title_mars_photo),
                selectedDate = state.selectedDate,
                maxDate = LocalDate.now(),
                onDateSelected = { selectedDate -> viewModel.onDateSelected(selectedDate) }
            )
        }
    ) { paddingValues ->
        SwipeableContent(
            currentDate = state.selectedDate,
            maxDate = LocalDate.now(),
            onDateChanged = { newDate -> viewModel.onDateSelected(newDate) },
            modifier = Modifier.padding(paddingValues)
        ) {
            MarsPhotoScreenContent(
                state = state,
                onRetry = { viewModel.onRefresh() },
                onRefresh = { viewModel.onRefresh() },
                onFullScreenPhoto = { marsPhoto ->
                    selectedMarsPhoto = marsPhoto
                    showFullScreenPhoto = true
                }
            )
        }
    }

    // Full screen photo dialog
    if (showFullScreenPhoto && selectedMarsPhoto != null) {
        FullScreenPhotoDialog(
            imageUrl = selectedMarsPhoto?.imageUrl,
            contentDescription = "Mars photo from ${selectedMarsPhoto?.rover?.roverName} rover",
            onDismiss = {
                showFullScreenPhoto = false
                selectedMarsPhoto = null
            },
            onShare = {
                selectedMarsPhoto?.let { marsPhoto ->
                    PhotoSharingUtils.shareMarsPhoto(context, marsPhoto)
                }
            }
        )
    }
}

@Composable
private fun MarsPhotoScreenContent(
    state: MarsPhotoState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onFullScreenPhoto: (MarsPhoto) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            pullToRefreshState.endRefresh()
        }
    }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.spacing_large))
        ) {
            // Display selected date
            Text(
                text = state.selectedDate.toDateString(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(bottom = dimensionResource(R.dimen.spacing_medium))
            )

            when {
                state.isLoading && state.filteredPhotos.isEmpty() -> {
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
                        Column(
                            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))
                        ) {
                            Text(
                                text = state.error,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(
                                modifier = Modifier.height(dimensionResource(R.dimen.spacing_small))
                            )

                            Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                        }
                    }
                }
                state.filteredPhotos.isNotEmpty() -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement =
                            Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                        horizontalArrangement =
                            Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                    ) {
                        items(state.filteredPhotos) { marsPhoto ->
                            MarsPhotoItem(
                                marsPhoto = marsPhoto,
                                onClick = { onFullScreenPhoto(marsPhoto) }
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
                                modifier =
                                    Modifier.height(dimensionResource(R.dimen.spacing_medium))
                            )

                            Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                        }
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
            onRefresh = {},
            onFullScreenPhoto = {}
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
            onRefresh = {},
            onFullScreenPhoto = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarsPhotoScreenEmptyPreview() {
    AstroAppTheme {
        MarsPhotoScreenContent(
            state = MarsPhotoState(isLoading = false, filteredPhotos = emptyList(), error = null),
            onRetry = {},
            onRefresh = {},
            onFullScreenPhoto = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarsPhotoScreenSuccessPreview() {
    AstroAppTheme {
        val samplePhotos =
            listOf(
                MarsPhoto(
                    id = 1,
                    sol = "2156",
                    imageUrl = "https://example.com/mars1.jpg",
                    earthDate = "2024-01-15",
                    camera = Camera(cameraName = "NAVCAM", cameraFullName = "Navigation Camera"),
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
                    earthDate = "2024-01-15",
                    camera = Camera(cameraName = "MAST", cameraFullName = "Mast Camera"),
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
                    earthDate = "2024-01-15",
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
                    earthDate = "2024-01-15",
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
            )
        MarsPhotoScreenContent(
            state =
                MarsPhotoState(
                    isLoading = false,
                    filteredPhotos = samplePhotos,
                    allMarsPhotos = samplePhotos,
                    selectedDate = "2024-01-15".toLocalDate(),
                    error = null
                ),
            onRetry = {},
            onRefresh = {},
            onFullScreenPhoto = {}
        )
    }
}

// MarsPhotoItem Previews
@Preview(name = "Mars Photo Item - Light", showBackground = true)
@Composable
private fun MarsPhotoItemLightPreview() {
    AstroAppTheme(themePreference = 0) {
        MarsPhotoItem(
            marsPhoto =
                MarsPhoto(
                    id = 1,
                    sol = "3654",
                    imageUrl = "https://example.com/mars-curiosity.jpg",
                    earthDate = "2024-01-15",
                    camera = Camera(cameraName = "NAVCAM", cameraFullName = "Navigation Camera"),
                    rover =
                        Rover(
                            roverName = "Curiosity",
                            launchDate = "2011-11-26",
                            landingDate = "2012-08-05"
                        )
                ),
            onClick = {}
        )
    }
}

@Preview(
    name = "Mars Photo Item - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun MarsPhotoItemDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        MarsPhotoItem(
            marsPhoto =
                MarsPhoto(
                    id = 1,
                    sol = "3654",
                    imageUrl = "https://example.com/mars-curiosity.jpg",
                    earthDate = "2024-01-15",
                    camera = Camera(cameraName = "NAVCAM", cameraFullName = "Navigation Camera"),
                    rover =
                        Rover(
                            roverName = "Curiosity",
                            launchDate = "2011-11-26",
                            landingDate = "2012-08-05"
                        )
                ),
            onClick = {}
        )
    }
}

@Preview(name = "Mars Photo Item Perseverance - Light", showBackground = true)
@Composable
private fun MarsPhotoItemPerseveranceLightPreview() {
    AstroAppTheme(themePreference = 0) {
        MarsPhotoItem(
            marsPhoto =
                MarsPhoto(
                    id = 2,
                    sol = "689",
                    imageUrl = "https://example.com/mars-perseverance.jpg",
                    earthDate = "2024-01-15",
                    camera = Camera(cameraName = "MAST", cameraFullName = "Mast Camera"),
                    rover =
                        Rover(
                            roverName = "Perseverance",
                            launchDate = "2020-07-30",
                            landingDate = "2021-02-18"
                        )
                ),
            onClick = {}
        )
    }
}

@Preview(
    name = "Mars Photo Item Perseverance - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun MarsPhotoItemPerseveranceDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        MarsPhotoItem(
            marsPhoto =
                MarsPhoto(
                    id = 2,
                    sol = "689",
                    imageUrl = "https://example.com/mars-perseverance.jpg",
                    earthDate = "2024-01-15",
                    camera = Camera(cameraName = "MAST", cameraFullName = "Mast Camera"),
                    rover =
                        Rover(
                            roverName = "Perseverance",
                            launchDate = "2020-07-30",
                            landingDate = "2021-02-18"
                        )
                ),
            onClick = {}
        )
    }
}
