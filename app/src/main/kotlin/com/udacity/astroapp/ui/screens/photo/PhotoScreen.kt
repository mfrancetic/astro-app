package com.udacity.astroapp.ui.screens.photo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.udacity.astroapp.ui.components.FullscreenVideoDialog
import com.udacity.astroapp.ui.components.YouTubeVideoPlayer
import com.udacity.astroapp.ui.components.VideoErrorContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.udacity.astroapp.R
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.utils.VideoUtils
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@RootNavGraph(start = true)
@Destination
@Composable
fun PhotoScreen(
    onShare: () -> Unit = {},
    onNavigateToFullScreen: (String) -> Unit = {},
    viewModel: PhotoViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PhotoSideEffect.ShowError -> {
                // Handle error display
            }
            is PhotoSideEffect.SharePhoto -> {
                // Handle photo sharing
            }
            is PhotoSideEffect.NavigateToFullScreen -> {
                onNavigateToFullScreen(sideEffect.photo.photoId.toString())
            }
            is PhotoSideEffect.ShowDatePicker -> {

            }
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.loadPhotos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.card_padding)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    Text(
                        text = state.error!!,
                        modifier = Modifier.padding(dimensionResource(R.dimen.card_padding)),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.card_padding)))

                Button(
                    onClick = { viewModel.loadPhotos() }
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
            state.selectedPhoto != null -> {
                PhotoContent(
                    photo = state.selectedPhoto!!,
                    onDateSelect = { date -> viewModel.loadPhotos() },
                    onShare = onShare,
                    onFullScreen = { onNavigateToFullScreen(state.selectedPhoto!!.photoUrl) }
                )
            }
        }
    }
}

@Composable
private fun PhotoContent(
    photo: Photo,
    onDateSelect: (String) -> Unit,
    onShare: () -> Unit,
    onFullScreen: () -> Unit
) {
    var showFullscreenVideo by remember { mutableStateOf(false) }
    val isVideo = VideoUtils.isVideoContent(photo.photoMediaType)

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))
        ) {
            Text(
                text = photo.photoTitle.ifEmpty { stringResource(R.string.no_title) },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
            )

            Text(
                text = photo.photoDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_medium))
            )

            // Image or video content
            if (isVideo) {
                VideoContent(
                    videoUrl = photo.photoUrl,
                    title = photo.photoTitle,
                    onFullscreenClick = { showFullscreenVideo = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.photo_content_height))
                )
            } else {
                ImageContent(
                    imageUrl = photo.photoUrl,
                    contentDescription = photo.photoTitle,
                    onFullScreen = onFullScreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.photo_content_height))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = photo.photoDescription.ifEmpty { stringResource(R.string.no_description) },
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                    Text(stringResource(R.string.share))
                }

                Button(onClick = { /* Open date picker */ }) {
                    Text(stringResource(R.string.select_date_button))
                }
            }
        }
    }

    // Fullscreen video dialog
    if (showFullscreenVideo && isVideo) {
        FullscreenVideoDialog(
            videoUrl = photo.photoUrl,
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
    Card(
        modifier = modifier.clickable { onFullScreen() }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun VideoContent(
    videoUrl: String,
    title: String,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    YouTubeVideoPlayer(
        videoUrl = videoUrl,
        title = title,
        modifier = modifier,
        showControls = true,
        onFullscreenClick = onFullscreenClick,
        onError = { /* Handle error if needed */ }
    )
}

