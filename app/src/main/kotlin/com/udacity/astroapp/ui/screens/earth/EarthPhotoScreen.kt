package com.udacity.astroapp.ui.screens.earth

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.skydoves.landscapist.glide.GlideImage
import com.udacity.astroapp.models.EarthPhoto
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import androidx.core.net.toUri
import androidx.compose.runtime.rememberCoroutineScope
import com.udacity.astroapp.utils.ImageSharingUtils
import kotlinx.coroutines.launch

@Destination
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthPhotoScreen(
    viewModel: EarthPhotoViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is EarthPhotoSideEffect.ShowError -> {
                // Error handled via state
            }
            is EarthPhotoSideEffect.SharePhoto -> {
                coroutineScope.launch {
                    ImageSharingUtils.shareImage(
                        context = context,
                        imageUrl = sideEffect.photo.earthPhotoUrl,
                        title = "Earth Photo",
                        text = "Check out this Earth photo:"
                    )
                }
            }
            is EarthPhotoSideEffect.OpenPhotoInFullscreen -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(sideEffect.photo.earthPhotoUrl.toUri(), "image/*")
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Could trigger error state in ViewModel
                }
            }
            EarthPhotoSideEffect.ShowDatePicker -> {
                // Date picker handled by state
            }
        }
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
                .padding(paddingValues)
                .padding(16.dp)
        ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Earth Photos",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Row {
                IconButton(onClick = { viewModel.onDatePickerClicked() }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                }
                
                IconButton(onClick = { viewModel.onGridModeToggled() }) {
                    Icon(
                        if (state.gridMode) Icons.Default.List else Icons.Default.GridView,
                        contentDescription = "Toggle View Mode"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Display
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Date: ${state.selectedDate}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading State
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Photo Content
        if (state.gridMode) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.photos) { photo ->
                    EarthPhotoGridItem(
                        photo = photo,
                        onPhotoClick = { viewModel.onPhotoClicked(photo) },
                        onShareClick = { viewModel.onSharePhotoClicked(photo) }
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.photos) { photo ->
                    EarthPhotoListItem(
                        photo = photo,
                        onPhotoClick = { viewModel.onPhotoClicked(photo) },
                        onShareClick = { viewModel.onSharePhotoClicked(photo) }
                    )
                }
            }
        }

        // Error State
        state.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        }
    }
}

@Composable
fun EarthPhotoGridItem(
    photo: EarthPhoto,
    onPhotoClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onPhotoClick
    ) {
        Box {
            GlideImage(
                imageModel = { photo.earthPhotoUrl },
                modifier = Modifier.fillMaxSize(),
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
                        Text("Failed to load")
                    }
                }
            )

            // Share button overlay
            IconButton(
                onClick = onShareClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun EarthPhotoListItem(
    photo: EarthPhoto,
    onPhotoClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onPhotoClick
    ) {
        Column {
            GlideImage(
                imageModel = { photo.earthPhotoUrl },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
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
                        Text("Failed to load image")
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = photo.earthPhotoIdentifier,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = photo.earthPhotoDateTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    photo.earthPhotoCaption?.let { caption ->
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                IconButton(onClick = onShareClick) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        }
    }
}