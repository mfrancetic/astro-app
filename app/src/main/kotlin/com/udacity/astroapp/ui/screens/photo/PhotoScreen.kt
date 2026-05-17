package com.udacity.astroapp.ui.screens.photo

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
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
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.ui.components.DirectVideoPlayer
import com.udacity.astroapp.ui.components.FullScreenPhotoDialog
import com.udacity.astroapp.ui.components.FullscreenVideoDialog
import com.udacity.astroapp.ui.components.MainTopAppBar
import com.udacity.astroapp.ui.components.SwipeableContent
import com.udacity.astroapp.ui.components.YouTubeVideoPlayer
import com.udacity.astroapp.ui.theme.AstroAppTheme
import com.udacity.astroapp.utils.PhotoSharingUtils
import com.udacity.astroapp.utils.VideoUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@RootNavGraph(start = true)
@Destination
@Composable
fun PhotoScreen(onShare: () -> Unit = {}, viewModel: PhotoViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    var showFullScreenPhoto by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PhotoSideEffect.ShowError -> {
                // Handle error display
            }
            is PhotoSideEffect.SharePhoto -> {
                PhotoSharingUtils.sharePhoto(context, sideEffect.photo)
            }
            is PhotoSideEffect.ShowDatePicker -> {}
        }
    }

    // Load initial data
    LaunchedEffect(Unit) { viewModel.loadPhotos() }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = stringResource(R.string.screen_title_photo),
                selectedDate =
                    if (state.selectedDate.isNotBlank()) {
                        LocalDate.parse(state.selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    } else {
                        null
                    },
                maxDate = LocalDate.now(),
                onDateSelected = { selectedDate ->
                    val dateString = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    viewModel.handleAction(PhotoAction.SelectDate(dateString))
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
            maxDate = LocalDate.now(),
            onDateChanged = { newDate ->
                val dateString = newDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                viewModel.handleAction(PhotoAction.SelectDate(dateString))
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            PhotoScreenContent(
                state = state,
                onRetry = { viewModel.loadPhotos() },
                onRefresh = { viewModel.loadPhotos() },
                onShare = onShare,
                onFullScreenPhoto = { photo ->
                    selectedPhoto = photo
                    showFullScreenPhoto = true
                },
                onSharePhoto = { photo -> viewModel.handleAction(PhotoAction.SharePhoto(photo)) }
            )
        }
    }

    // Full screen photo dialog
    if (showFullScreenPhoto && selectedPhoto != null) {
        FullScreenPhotoDialog(
            imageUrl = selectedPhoto?.photoUrl,
            contentDescription = selectedPhoto?.photoTitle,
            onDismiss = {
                showFullScreenPhoto = false
                selectedPhoto = null
            },
            onShare = {
                selectedPhoto?.let { photo ->
                    viewModel.handleAction(PhotoAction.SharePhoto(photo))
                }
            }
        )
    }
}

@Composable
private fun PhotoContent(photo: Photo, onShare: () -> Unit, onFullScreen: () -> Unit) {
    var showFullscreenVideo by remember { mutableStateOf(false) }
    val isVideo = VideoUtils.isVideoContent(photo.photoMediaType ?: "")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))) {
            photo.photoTitle?.let {
                Text(
                    text = it.ifEmpty { stringResource(R.string.no_title) },
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
                )
            }

            // Image or video content
            if (isVideo) {
                VideoContent(
                    videoUrl = photo.photoUrl ?: "",
                    title = photo.photoTitle ?: "",
                    onFullscreenClick = { showFullscreenVideo = true },
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(dimensionResource(R.dimen.photo_content_height))
                )
            } else {
                ImageContent(
                    imageUrl = photo.photoUrl ?: "",
                    contentDescription = photo.photoTitle ?: "",
                    onFullScreen = onFullScreen,
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(dimensionResource(R.dimen.photo_content_height))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            photo.photoDescription?.let {
                Text(
                    text = it.ifEmpty { stringResource(R.string.no_description) },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Fullscreen video dialog
    if (showFullscreenVideo && isVideo) {
        FullscreenVideoDialog(
            videoUrl = photo.photoUrl ?: "",
            onDismiss = { showFullscreenVideo = false }
        )
    }
}

@Composable
private fun ImageContent(
    imageUrl: String,
    contentDescription: String,
    onFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.clickable { onFullScreen() }) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun PhotoScreenContent(
    state: PhotoState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onShare: () -> Unit,
    onFullScreenPhoto: (Photo) -> Unit,
    onSharePhoto: (Photo) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            pullToRefreshState.endRefresh()
        }
    }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(dimensionResource(R.dimen.card_padding)),
            horizontalAlignment = Alignment.CenterHorizontally
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
                state.isLoading && state.photos.isEmpty() -> {
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
                            modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))
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
                state.photos.isNotEmpty() -> {
                    // Show the first photo if no photo is selected, or the selected photo
                    val photoToShow = state.selectedPhoto ?: state.photos.first()
                    PhotoContent(
                        photo = photoToShow,
                        onShare = onShare,
                        onFullScreen = { onFullScreenPhoto(photoToShow) }
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.no_photo_found),
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
        } // Close Column

        // FAB for sharing current photo
        val showFab = !state.isLoading && state.error == null && state.photos.isNotEmpty()
        if (showFab) {
            val currentPhoto = state.selectedPhoto ?: state.photos.first()
            FloatingActionButton(
                onClick = { onSharePhoto(currentPhoto) },
                modifier =
                    Modifier.align(Alignment.BottomEnd)
                        .padding(dimensionResource(R.dimen.spacing_large))
            ) {
                Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.share))
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
private fun VideoContent(
    videoUrl: String,
    title: String,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    if (hasError) {
        Card(
            modifier = modifier,
            colors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.card_padding)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Video Error",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                Button(
                    onClick = {
                        hasError = false
                        errorMessage = ""
                    }
                ) {
                    Text("Retry")
                }
            }
        }
    } else {
        if (VideoUtils.isYouTubeUrl(videoUrl)) {
            YouTubeVideoPlayer(videoUrl = videoUrl, modifier = modifier.fillMaxSize())
        } else {
            DirectVideoPlayer(
                videoUrl = videoUrl,
                modifier = modifier.fillMaxSize(),
                onError = { error ->
                    hasError = true
                    errorMessage = error
                },
            )
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
private fun PhotoScreenLoadingPreview() {
    AstroAppTheme {
        PhotoScreenContent(
            state = PhotoState(isLoading = true),
            onRetry = {},
            onRefresh = {},
            onShare = {},
            onFullScreenPhoto = {},
            onSharePhoto = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoScreenErrorPreview() {
    AstroAppTheme {
        PhotoScreenContent(
            state =
                PhotoState(
                    isLoading = false,
                    error = "Failed to load photos. Please check your internet connection."
                ),
            onRetry = {},
            onRefresh = {},
            onShare = {},
            onFullScreenPhoto = {},
            onSharePhoto = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoScreenEmptyPreview() {
    AstroAppTheme {
        PhotoScreenContent(
            state = PhotoState(isLoading = false, photos = emptyList(), error = null),
            onRetry = {},
            onRefresh = {},
            onShare = {},
            onFullScreenPhoto = {},
            onSharePhoto = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoScreenSuccessPreview() {
    AstroAppTheme {
        val samplePhotos =
            listOf(
                Photo(
                    photoId = 1,
                    photoTitle = "Nebula in Orion",
                    photoDate = "2024-01-15",
                    photoDescription =
                        "A stunning view of the Orion Nebula captured by the Hubble Space Telescope. This stellar nursery is located about 1,344 light-years away from Earth.",
                    photoUrl = "https://example.com/nebula.jpg",
                    photoMediaType = "image"
                )
            )
        PhotoScreenContent(
            state =
                PhotoState(
                    isLoading = false,
                    photos = samplePhotos,
                    allPhotos = samplePhotos,
                    selectedDate = "2024-01-15",
                    error = null
                ),
            onRetry = {},
            onRefresh = {},
            onShare = {},
            onFullScreenPhoto = {},
            onSharePhoto = {}
        )
    }
}

// PhotoContent Previews
@Preview(name = "Photo Content - Light", showBackground = true)
@Composable
private fun PhotoContentLightPreview() {
    AstroAppTheme(themePreference = 0) {
        PhotoContent(
            photo =
                Photo(
                    photoId = 1,
                    photoTitle = "The Helix Nebula",
                    photoDate = "2024-01-15",
                    photoDescription =
                        "The Helix Nebula is a large planetary nebula located in the constellation Aquarius. Also known as NGC 7293, it is one of the closest bright nebulae to Earth at a distance of about 695 light-years.",
                    photoUrl = "https://example.com/helix-nebula.jpg",
                    photoMediaType = "image"
                ),
            onShare = {},
            onFullScreen = {}
        )
    }
}

@Preview(
    name = "Photo Content - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PhotoContentDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        PhotoContent(
            photo =
                Photo(
                    photoId = 1,
                    photoTitle = "The Helix Nebula",
                    photoDate = "2024-01-15",
                    photoDescription =
                        "The Helix Nebula is a large planetary nebula located in the constellation Aquarius. Also known as NGC 7293, it is one of the closest bright nebulae to Earth at a distance of about 695 light-years.",
                    photoUrl = "https://example.com/helix-nebula.jpg",
                    photoMediaType = "image"
                ),
            onShare = {},
            onFullScreen = {}
        )
    }
}

@Preview(name = "Photo Content Video - Light", showBackground = true)
@Composable
private fun PhotoContentVideoLightPreview() {
    AstroAppTheme(themePreference = 0) {
        PhotoContent(
            photo =
                Photo(
                    photoId = 2,
                    photoTitle = "Solar Eclipse Time-Lapse",
                    photoDate = "2024-04-08",
                    photoDescription =
                        "A mesmerizing time-lapse of the total solar eclipse showing the Moon's shadow sweeping across the Earth's surface. Captured from multiple vantage points.",
                    photoUrl = "https://www.youtube.com/watch?v=example",
                    photoMediaType = "video"
                ),
            onShare = {},
            onFullScreen = {}
        )
    }
}

@Preview(
    name = "Photo Content Video - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PhotoContentVideoDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        PhotoContent(
            photo =
                Photo(
                    photoId = 2,
                    photoTitle = "Solar Eclipse Time-Lapse",
                    photoDate = "2024-04-08",
                    photoDescription =
                        "A mesmerizing time-lapse of the total solar eclipse showing the Moon's shadow sweeping across the Earth's surface. Captured from multiple vantage points.",
                    photoUrl = "https://www.youtube.com/watch?v=example",
                    photoMediaType = "video"
                ),
            onShare = {},
            onFullScreen = {}
        )
    }
}

// ImageContent Previews
@Preview(name = "Image Content - Light", showBackground = true)
@Composable
private fun ImageContentLightPreview() {
    AstroAppTheme(themePreference = 0) {
        ImageContent(
            imageUrl = "https://example.com/andromeda-galaxy.jpg",
            contentDescription = "The Andromeda Galaxy",
            onFullScreen = {},
            modifier =
                Modifier.fillMaxWidth().height(dimensionResource(R.dimen.photo_content_height))
        )
    }
}

@Preview(
    name = "Image Content - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ImageContentDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        ImageContent(
            imageUrl = "https://example.com/andromeda-galaxy.jpg",
            contentDescription = "The Andromeda Galaxy",
            onFullScreen = {},
            modifier =
                Modifier.fillMaxWidth().height(dimensionResource(R.dimen.photo_content_height))
        )
    }
}

// VideoContent Previews
@Preview(name = "Video Content - Light", showBackground = true)
@Composable
private fun VideoContentLightPreview() {
    AstroAppTheme(themePreference = 0) {
        VideoContent(
            videoUrl = "https://www.youtube.com/watch?v=example",
            title = "Journey Through the Solar System",
            onFullscreenClick = {},
            modifier =
                Modifier.fillMaxWidth().height(dimensionResource(R.dimen.photo_content_height))
        )
    }
}

@Preview(
    name = "Video Content - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun VideoContentDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        VideoContent(
            videoUrl = "https://www.youtube.com/watch?v=example",
            title = "Journey Through the Solar System",
            onFullscreenClick = {},
            modifier =
                Modifier.fillMaxWidth().height(dimensionResource(R.dimen.photo_content_height))
        )
    }
}
