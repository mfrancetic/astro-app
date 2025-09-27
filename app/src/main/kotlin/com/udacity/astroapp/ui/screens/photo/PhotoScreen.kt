package com.udacity.astroapp.ui.screens.photo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.ui.components.DatePickerButton
import com.udacity.astroapp.ui.components.FullScreenPhotoDialog
import com.udacity.astroapp.ui.components.FullscreenVideoDialog
import com.udacity.astroapp.ui.components.YouTubeVideoPlayer
import com.udacity.astroapp.ui.theme.AstroAppTheme
import com.udacity.astroapp.utils.VideoUtils
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@RootNavGraph(start = true)
@Destination
@Composable
fun PhotoScreen(onShare: () -> Unit = {}, viewModel: PhotoViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    var showFullScreenPhoto by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PhotoSideEffect.ShowError -> {
                // Handle error display
            }
            is PhotoSideEffect.SharePhoto -> {
                // Handle photo sharing
            }
            is PhotoSideEffect.ShowDatePicker -> {}
        }
    }

    // Load initial data
    LaunchedEffect(Unit) { viewModel.loadPhotos() }

    PhotoScreenContent(
        state = state,
        onRetry = { viewModel.loadPhotos() },
        onShare = onShare,
        onFullScreenPhoto = { photo ->
            selectedPhoto = photo
            showFullScreenPhoto = true
        },
        onDateSelected = { selectedDate ->
            viewModel.handleAction(PhotoAction.SelectDate(selectedDate))
        }
    )

    // Full screen photo dialog
    if (showFullScreenPhoto && selectedPhoto != null) {
        FullScreenPhotoDialog(
            imageUrl = selectedPhoto?.photoUrl,
            contentDescription = selectedPhoto?.photoTitle,
            onDismiss = {
                showFullScreenPhoto = false
                selectedPhoto = null
            },
            onShare = onShare
        )
    }
}

@Composable
private fun PhotoContent(
    photo: Photo,
    selectedDate: String?,
    onDateSelect: (String) -> Unit,
    onShare: () -> Unit,
    onFullScreen: () -> Unit
) {
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

            photo.photoDate.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_medium))
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

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onShare, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                    Text(stringResource(R.string.share))
                }

                DatePickerButton(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelect,
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.select_date_button)
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
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun PhotoScreenContent(
    state: PhotoState,
    onRetry: () -> Unit,
    onShare: () -> Unit,
    onFullScreenPhoto: (Photo) -> Unit,
    onDateSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.card_padding)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Filter section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DatePickerButton(
                        selectedDate = state.selectedDate.ifBlank { null },
                        onDateSelected = onDateSelected,
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.select_date_button)
                    )
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
                    Column(modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))) {
                        Text(text = state.error, color = MaterialTheme.colorScheme.onErrorContainer)

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                    }
                }
            }
            state.photos.isNotEmpty() -> {
                // Show the first photo if no photo is selected, or the selected photo
                val photoToShow = state.selectedPhoto ?: state.photos.first()
                PhotoContent(
                    photo = photoToShow,
                    selectedDate = state.selectedDate.takeIf { it.isNotBlank() },
                    onDateSelect = onDateSelected,
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
                modifier = Modifier.padding(dimensionResource(R.dimen.card_padding)),
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
        YouTubeVideoPlayer(
            videoUrl = videoUrl,
            modifier = modifier,
            onFullscreenClick = onFullscreenClick,
            onError = { error ->
                hasError = true
                errorMessage = error
            }
        )
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
            onShare = {},
            onFullScreenPhoto = {},
            onDateSelected = {}
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
            onShare = {},
            onFullScreenPhoto = {},
            onDateSelected = {}
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
            onShare = {},
            onFullScreenPhoto = {},
            onDateSelected = {}
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
            onShare = {},
            onFullScreenPhoto = {},
            onDateSelected = {}
        )
    }
}
