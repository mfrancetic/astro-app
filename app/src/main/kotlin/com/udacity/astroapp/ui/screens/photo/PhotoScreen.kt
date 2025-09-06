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
import androidx.compose.ui.layout.ContentScale
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

@RootNavGraph(start = true)
@Destination
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(
    viewModel: PhotoViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PhotoSideEffect.ShowError -> {
                // Handle error - could show snackbar
            }
            is PhotoSideEffect.SharePhoto -> {
                // Handle photo sharing
            }
            is PhotoSideEffect.OpenPhotoInFullscreen -> {
                // Handle fullscreen view
            }
            PhotoSideEffect.ShowDatePicker -> {
                // Handle date picker
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                        contentDescription = stringResource(R.string.cd_calendar)
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
                            imageModel = url,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensionResource(R.dimen.photo_detail_height)),
                            contentScale = ContentScale.Crop,
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