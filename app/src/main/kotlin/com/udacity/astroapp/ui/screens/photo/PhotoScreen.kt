package com.udacity.astroapp.ui.screens.photo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.skydoves.landscapist.glide.GlideImage
import com.udacity.astroapp.R
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.udacity.astroapp.utils.ImageSharingUtils
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.skydoves.landscapist.ImageOptions
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.ui.components.AstroDatePickerDialog

@RootNavGraph(start = true)
@Destination
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(
    viewModel: PhotoViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var fullScreenPhoto by rememberSaveable { mutableStateOf<Photo?>(null) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PhotoSideEffect.ShowError -> {
                // Error will be shown via snackbar using a state
            }

            is PhotoSideEffect.SharePhoto -> {
                coroutineScope.launch {
                    sideEffect.photo.photoUrl?.let { url ->
                        ImageSharingUtils.shareImage(
                            context = context,
                            imageUrl = url,
                            title = sideEffect.photo.photoTitle,
                            text = "Check out this astronomy photo:"
                        )
                    }
                }
            }

            is PhotoSideEffect.OpenPhotoInFullscreen -> {
                fullScreenPhoto = sideEffect.photo
            }

            PhotoSideEffect.ShowDatePicker -> {
                // Date picker will be shown by the state management
            }
        }
    }

    // Date picker dialog
    if (state.showDatePicker) {
        AstroDatePickerDialog(
            onDateSelected = { selectedDate ->
                viewModel.onDateSelected(selectedDate)
            },
            onDismiss = { viewModel.onDatePickerDismissed() },
            date = state.selectedDate
        )
    }

    fullScreenPhoto?.photoUrl?.let { url ->
        FullScreenPhotoDialog(
            photoUrl = url,
            photoTitle = fullScreenPhoto?.photoTitle,
            onDismiss = {
                fullScreenPhoto = null
            }
        )
    }

    // Handle error state from ViewModel
    state.error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            // Top App Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.title_astronomy_picture_of_day),
                    style = MaterialTheme.typography.headlineSmall
                )

                Row {
                    IconButton(onClick = { viewModel.onDatePickerClicked() }) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = stringResource(R.string.calendar)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.onSharePhotoClicked() },
                        enabled = state.photo != null
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(R.string.cd_share)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            // Loading State
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.loading_container_height)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Photo Content
            state.photo?.let { photo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.spacing_small)),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = dimensionResource(R.dimen.card_elevation)
                    )
                ) {
                    Column {
                        // Photo Image
                        photo.photoUrl?.let { url ->
                            GlideImage(
                                imageModel = { url },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimensionResource(R.dimen.photo_detail_height))
                                    .clickable { viewModel.onPhotoClicked() },
                                loading = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                },
                                failure = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(stringResource(R.string.error_failed_to_load_image))
                                    }
                                }
                            )
                        }

                        // Photo Details
                        Column(
                            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
                        ) {
                            photo.photoTitle?.let { title ->
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(
                                        bottom = dimensionResource(R.dimen.spacing_small)
                                    )
                                )
                            }

                            Text(
                                text = photo.photoDate,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    bottom = dimensionResource(R.dimen.spacing_small)
                                )
                            )

                            photo.photoDescription?.let { description ->
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Justify
                                )
                            }
                        }
                    }
                }
            }

            // Error State
            state.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.spacing_small)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium)),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun FullScreenPhotoDialog(
    photoUrl: String,
    photoTitle: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismiss() }
                    )
                }
        ) {
            GlideImage(
                imageModel = { photoUrl },
                imageOptions = ImageOptions(alignment = Alignment.Center, contentScale = ContentScale.Fit),
                loading = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                },
                failure = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Failed to load image",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            )

            // Close button in top-right corner
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}