package com.udacity.astroapp.ui.screens.earthphoto

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
import com.udacity.astroapp.models.EarthPhoto
import com.udacity.astroapp.ui.components.CardComponent
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

@Destination
@Composable
fun EarthPhotoScreen(viewModel: EarthPhotoViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    var showFullScreenPhoto by remember { mutableStateOf(false) }
    var selectedEarthPhoto by remember { mutableStateOf<EarthPhoto?>(null) }

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

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = stringResource(R.string.screen_title_earth_photo),
                selectedDate =
                    if (state.selectedDate.isNotBlank()) {
                        LocalDate.parse(state.selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    } else {
                        null
                    },
                maxDate = LocalDate.parse(state.maxAvailableDate, DateTimeFormatter.ISO_LOCAL_DATE),
                onDateSelected = { selectedDate ->
                    val dateString = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    viewModel.handleAction(EarthPhotoAction.SelectDate(dateString))
                }
            )
        }
    ) { paddingValues ->
        SwipeableContent(
            currentDate =
                if (state.selectedDate.isNotBlank()) {
                    LocalDate.parse(state.selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
                } else {
                    LocalDate.now()
                },
            maxDate = LocalDate.parse(state.maxAvailableDate, DateTimeFormatter.ISO_LOCAL_DATE),
            onDateChanged = { newDate ->
                val dateString = newDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                viewModel.handleAction(EarthPhotoAction.SelectDate(dateString))
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            EarthPhotoScreenContent(
                state = state,
                onRetry = { viewModel.loadPhotos() },
                onRefresh = { viewModel.loadPhotos() },
                onFullScreenPhoto = { earthPhoto ->
                    selectedEarthPhoto = earthPhoto
                    showFullScreenPhoto = true
                }
            )
        }
    }

    // Full screen photo dialog
    if (showFullScreenPhoto && selectedEarthPhoto != null) {
        FullScreenPhotoDialog(
            imageUrl = selectedEarthPhoto?.earthPhotoUrl,
            contentDescription = "Earth photo from ${selectedEarthPhoto?.earthPhotoDateTime}",
            onDismiss = {
                showFullScreenPhoto = false
                selectedEarthPhoto = null
            },
            onShare = {
                selectedEarthPhoto?.let { earthPhoto ->
                    PhotoSharingUtils.shareEarthPhoto(context, earthPhoto)
                }
            }
        )
    }
}

@Composable
private fun EarthPhotoScreenContent(
    state: EarthPhotoState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onFullScreenPhoto: (EarthPhoto) -> Unit
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
            if (state.selectedDate.isNotBlank()) {
                Text(
                    text = state.selectedDate,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(bottom = dimensionResource(R.dimen.spacing_medium))
                )
            }

            when {
                state.isLoading && state.earthPhotos.isEmpty() -> {
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
                        }
                    }
                }
                state.earthPhotos.isNotEmpty() -> {
                    Column {
                        state.earthPhotos.firstOrNull()?.earthPhotoCaption?.let { caption ->
                            CardComponent {
                                Text(caption, style = MaterialTheme.typography.titleMedium)
                            }
                        }

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
                                    onClick = { onFullScreenPhoto(earthPhoto) }
                                )
                            }
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
private fun EarthPhotoItem(earthPhoto: EarthPhoto, onClick: (EarthPhoto) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().aspectRatio(1f), onClick = { onClick(earthPhoto) }) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = earthPhoto.earthPhotoUrl,
                contentDescription = "Earth photo from ${earthPhoto.earthPhotoDateTime}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay with time
            Surface(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Text(
                    text = earthPhoto.earthPhotoDateTime.substringAfter(" "),
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
            onRefresh = {},
            onFullScreenPhoto = {}
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
            onRefresh = {},
            onFullScreenPhoto = {}
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
            onRefresh = {},
            onFullScreenPhoto = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EarthPhotoScreenSuccessPreview() {
    AstroAppTheme {
        val samplePhotos =
            listOf(
                EarthPhoto(
                    earthPhotoId = 1,
                    earthPhotoUrl = "https://example.com/earth1.jpg",
                    earthPhotoDateTime = "2024-01-15 12:30:45",
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
            )
        EarthPhotoScreenContent(
            state =
                EarthPhotoState(
                    isLoading = false,
                    earthPhotos = samplePhotos,
                    allEarthPhotos = samplePhotos,
                    selectedDate = "2024-01-15",
                    error = null
                ),
            onRetry = {},
            onRefresh = {},
            onFullScreenPhoto = {}
        )
    }
}

// EarthPhotoItem Previews
@Preview(name = "Earth Photo Item - Light", showBackground = true)
@Composable
private fun EarthPhotoItemLightPreview() {
    AstroAppTheme(themePreference = 0) {
        EarthPhotoItem(
            earthPhoto =
                EarthPhoto(
                    earthPhotoId = 1,
                    earthPhotoUrl = "https://example.com/earth-pacific.jpg",
                    earthPhotoDateTime = "2024-01-15 14:23:15"
                ),
            onClick = {}
        )
    }
}

@Preview(
    name = "Earth Photo Item - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun EarthPhotoItemDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        EarthPhotoItem(
            earthPhoto =
                EarthPhoto(
                    earthPhotoId = 1,
                    earthPhotoUrl = "https://example.com/earth-pacific.jpg",
                    earthPhotoDateTime = "2024-01-15 14:23:15"
                ),
            onClick = {}
        )
    }
}
