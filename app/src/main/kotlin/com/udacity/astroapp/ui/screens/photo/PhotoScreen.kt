package com.udacity.astroapp.ui.screens.photo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.udacity.astroapp.R
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
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.loadTodayPhoto()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.loadTodayPhoto() }
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
            state.photo != null -> {
                PhotoContent(
                    photo = state.photo!!,
                    onDateSelect = { date -> viewModel.loadPhotoForDate(date) },
                    onShare = onShare,
                    onFullScreen = { onNavigateToFullScreen(state.photo!!.url ?: "") }
                )
            }
        }
    }
}

@Composable
private fun PhotoContent(
    photo: com.udacity.astroapp.data.models.Photo,
    onDateSelect: (String) -> Unit,
    onShare: () -> Unit,
    onFullScreen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = photo.title ?: stringResource(R.string.no_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = photo.date ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Image or video placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Image/Video content to be implemented")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = photo.explanation ?: stringResource(R.string.no_description),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onShare) {
                    Text(stringResource(R.string.share))
                }

                Button(onClick = onFullScreen) {
                    Text("Full Screen")
                }

                Button(onClick = { /* Open date picker */ }) {
                    Text("Select Date")
                }
            }
        }
    }
}