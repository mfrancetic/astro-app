package com.udacity.astroapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.udacity.astroapp.R
import com.udacity.astroapp.ui.theme.AstroAppTheme

@Composable
fun AstroImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    cornerRadius: Dp = dimensionResource(R.dimen.card_corner_radius),
    onImageClick: (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (onImageClick != null) {
                        Modifier.clickable { onImageClick() }
                    } else {
                        Modifier
                    }
                ),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(imageUrl).crossfade(true).build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onState = { state ->
                    when (state) {
                        is AsyncImagePainter.State.Loading -> {
                            isLoading = true
                            isError = false
                        }
                        is AsyncImagePainter.State.Success -> {
                            isLoading = false
                            isError = false
                        }
                        is AsyncImagePainter.State.Error -> {
                            isLoading = false
                            isError = true
                        }
                        else -> Unit
                    }
                }
            )

            // Loading overlay
            if (isLoading) {
                placeholder?.invoke() ?: DefaultImagePlaceholder()
            }

            // Error overlay
            if (isError) {
                error?.invoke() ?: DefaultImageError()
            }
        } else {
            // No image URL provided
            error?.invoke() ?: DefaultImageError()
        }
    }
}

@Composable
fun PhotoCard(
    imageUrl: String?,
    title: String?,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onImageClick: (() -> Unit)? = null,
    onCardClick: (() -> Unit)? = null
) {
    Card(
        modifier =
            modifier.then(
                if (onCardClick != null) {
                    Modifier.clickable { onCardClick() }
                } else {
                    Modifier
                }
            )
    ) {
        Column {
            AstroImage(
                imageUrl = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                onImageClick = onImageClick
            )

            if (title != null || subtitle != null) {
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.card_inner_padding))) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2
                        )
                    }

                    if (subtitle != null) {
                        Spacer(
                            modifier =
                                Modifier.height(dimensionResource(R.dimen.spacing_extra_small))
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridImageItem(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    overlayText: String? = null,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = modifier.aspectRatio(1f)) {
        AstroImage(
            imageUrl = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            onImageClick = onClick
        )

        // Overlay text
        if (overlayText != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Text(
                    text = overlayText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier =
                        Modifier.padding(dimensionResource(R.dimen.earth_photo_overlay_padding)),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun DefaultImagePlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(dimensionResource(R.dimen.loading_indicator_small_size)),
                strokeWidth = dimensionResource(R.dimen.loading_indicator_small_stroke)
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.image_placeholder_spacing)))
            Icon(
                Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(dimensionResource(R.dimen.image_placeholder_icon_size))
            )
        }
    }
}

@Composable
private fun DefaultImageError() {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.BrokenImage,
            contentDescription = stringResource(R.string.failed_to_load_image),
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(dimensionResource(R.dimen.image_placeholder_icon_size))
        )
    }
}

@Composable
fun FullScreenImageViewer(
    imageUrl: String?,
    contentDescription: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().background(Color.Black).clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model =
                    ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = stringResource(R.string.image_not_available),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun FullScreenPhotoDialog(
    imageUrl: String?,
    contentDescription: String?,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Share button
                IconButton(
                    onClick = onShare,
                    modifier =
                        Modifier.background(
                            Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = stringResource(R.string.share),
                        tint = Color.White
                    )
                }

                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier =
                        Modifier.background(
                            Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.navigation_drawer_close),
                        tint = Color.White
                    )
                }
            }
            // Main image content
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().clickable { onDismiss() }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.BrokenImage,
                            contentDescription = stringResource(R.string.failed_to_load_image),
                            tint = Color.White,
                            modifier =
                                Modifier.size(
                                    dimensionResource(R.dimen.image_placeholder_icon_size)
                                )
                        )
                        Spacer(
                            modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium))
                        )
                        Text(
                            text = stringResource(R.string.image_not_available),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

// Image Components Previews
@Preview(name = "AstroImage - Light", showBackground = true)
@Composable
private fun AstroImageLightPreview() {
    AstroAppTheme(themePreference = 0) {
        AstroImage(
            imageUrl = "https://example.com/nebula.jpg",
            contentDescription = "Beautiful nebula",
            modifier =
                Modifier.fillMaxWidth().height(dimensionResource(R.dimen.photo_content_height))
        )
    }
}

@Preview(
    name = "AstroImage - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AstroImageDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        AstroImage(
            imageUrl = "https://example.com/nebula.jpg",
            contentDescription = "Beautiful nebula",
            modifier =
                Modifier.fillMaxWidth().height(dimensionResource(R.dimen.photo_content_height))
        )
    }
}

@Preview(name = "PhotoCard - Light", showBackground = true)
@Composable
private fun PhotoCardLightPreview() {
    AstroAppTheme(themePreference = 0) {
        PhotoCard(
            imageUrl = "https://example.com/galaxy.jpg",
            title = "Andromeda Galaxy",
            subtitle = "Our closest galactic neighbor",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "PhotoCard - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PhotoCardDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        PhotoCard(
            imageUrl = "https://example.com/galaxy.jpg",
            title = "Andromeda Galaxy",
            subtitle = "Our closest galactic neighbor",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "GridImageItem with overlay - Light", showBackground = true)
@Composable
private fun GridImageItemLightPreview() {
    AstroAppTheme(themePreference = 0) {
        GridImageItem(
            imageUrl = "https://example.com/mars-surface.jpg",
            contentDescription = "Mars surface photo",
            overlayText = "Sol 3654 - NAVCAM",
        )
    }
}

@Preview(
    name = "GridImageItem with overlay - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GridImageItemDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        GridImageItem(
            imageUrl = "https://example.com/mars-surface.jpg",
            contentDescription = "Mars surface photo",
            overlayText = "Sol 3654 - NAVCAM",
        )
    }
}

@Preview(name = "GridImageItem no overlay - Light", showBackground = true)
@Composable
private fun GridImageItemNoOverlayLightPreview() {
    AstroAppTheme(themePreference = 0) {
        GridImageItem(
            imageUrl = "https://example.com/earth.jpg",
            contentDescription = "Earth photo",
        )
    }
}

@Preview(
    name = "GridImageItem no overlay - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GridImageItemNoOverlayDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        GridImageItem(
            imageUrl = "https://example.com/earth.jpg",
            contentDescription = "Earth photo",
        )
    }
}

@Preview(name = "FullScreenImageViewer - Light", showBackground = true)
@Composable
private fun FullScreenImageViewerLightPreview() {
    AstroAppTheme(themePreference = 0) {
        FullScreenImageViewer(
            imageUrl = "https://example.com/jupiter.jpg",
            contentDescription = "Jupiter close-up",
            onClose = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(
    name = "FullScreenImageViewer - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FullScreenImageViewerDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        FullScreenImageViewer(
            imageUrl = "https://example.com/jupiter.jpg",
            contentDescription = "Jupiter close-up",
            onClose = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
