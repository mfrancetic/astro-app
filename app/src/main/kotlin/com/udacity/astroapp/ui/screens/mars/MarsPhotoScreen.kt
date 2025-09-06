package com.udacity.astroapp.ui.screens.mars

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.glide.GlideImage
import com.udacity.astroapp.models.MarsPhoto
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarsPhotoScreen(
    viewModel: MarsPhotoViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MarsPhotoSideEffect.ShowError -> {
                // Handle error
            }
            is MarsPhotoSideEffect.SharePhoto -> {
                // Handle photo sharing
            }
            is MarsPhotoSideEffect.OpenPhotoInFullscreen -> {
                // Handle fullscreen view
            }
            MarsPhotoSideEffect.ShowDatePicker -> {
                // Handle date picker
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mars Rover Photos",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Row {
                IconButton(onClick = { viewModel.onDatePickerClicked() }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                }
                
                IconButton(onClick = { viewModel.onRefresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rover Selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Rover",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.availableRovers.forEach { rover ->
                        FilterChip(
                            selected = state.selectedRover == rover,
                            onClick = { viewModel.onRoverSelected(rover) },
                            label = { Text(rover.capitalize()) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sol Input
        OutlinedTextField(
            value = state.selectedSol,
            onValueChange = { viewModel.onSolChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Sol (Martian day)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            supportingText = { Text("Enter a Martian sol number") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        if (state.selectedDate.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Earth Date: ${state.selectedDate}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

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

        // Photos Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.photos) { photo ->
                MarsPhotoItem(
                    photo = photo,
                    onPhotoClick = { viewModel.onPhotoClicked(photo) },
                    onShareClick = { viewModel.onSharePhotoClicked(photo) }
                )
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

@Composable
fun MarsPhotoItem(
    photo: MarsPhoto,
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
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                photo.imageUrl?.let { url ->
                    GlideImage(
                        imageModel = url,
                        modifier = Modifier.fillMaxSize(),
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
                                Text("Failed to load")
                            }
                        }
                    )
                }
                
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
            
            // Photo Info
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                photo.camera?.cameraName?.let { cameraName ->
                    Text(
                        text = cameraName,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
                
                photo.sol?.let { sol ->
                    Text(
                        text = "Sol $sol",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
                
                photo.earthDate?.let { earthDate ->
                    Text(
                        text = earthDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}