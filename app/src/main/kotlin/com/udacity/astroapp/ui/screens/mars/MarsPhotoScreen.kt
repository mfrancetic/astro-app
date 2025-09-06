package com.udacity.astroapp.ui.screens.mars

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.skydoves.landscapist.glide.GlideImage
import com.udacity.astroapp.models.MarsPhoto
import com.udacity.astroapp.ui.components.SearchTextField
import com.udacity.astroapp.utils.ImageSharingUtils
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import androidx.core.net.toUri
import java.util.Locale

@Destination
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarsPhotoScreen(
    viewModel: MarsPhotoViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MarsPhotoSideEffect.ShowError -> {
                // Error handled via state
            }
            is MarsPhotoSideEffect.SharePhoto -> {
                coroutineScope.launch {
                    sideEffect.photo.imageUrl?.let { url ->
                        ImageSharingUtils.shareImage(
                            context = context,
                            imageUrl = url,
                            title = "Mars Photo",
                            text = "Check out this Mars photo:"
                        )
                    }
                }
            }
            is MarsPhotoSideEffect.OpenPhotoInFullscreen -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(sideEffect.photo.imageUrl?.toUri(), "image/*")
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Could trigger error state in ViewModel
                }
            }
            MarsPhotoSideEffect.ShowDatePicker -> {
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
                            label = { Text(rover.replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                                Locale.ROOT
                            ) else it.toString() }) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SearchTextField(
            initialValue = state.selectedSol,
            onValueChange = { viewModel.onSolChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            label = "Sol (Martian day)",
            placeholder = "Enter a Martian sol number",
            searchContentDescription = "Sol input",
            clearContentDescription = "Clear sol",
            keyboardType = KeyboardType.Number
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
                        imageModel = { url },
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