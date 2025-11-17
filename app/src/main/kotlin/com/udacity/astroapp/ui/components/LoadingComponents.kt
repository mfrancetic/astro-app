package com.udacity.astroapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.udacity.astroapp.R
import com.udacity.astroapp.ui.theme.AstroAppTheme

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier, text: String? = null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()

            if (text != null) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ErrorCard(errorMessage: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                Button(
                    onClick = onRetry,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier =
                Modifier.padding(dimensionResource(R.dimen.spacing_extra_large)).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                Button(onClick = onAction) { Text(actionText) }
            }
        }
    }
}

@Composable
fun FullScreenLoading(
    text: String = stringResource(R.string.loading),
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier.fillMaxSize().padding(dimensionResource(R.dimen.fullscreen_loading_padding)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(dimensionResource(R.dimen.fullscreen_loading_indicator_size)),
                strokeWidth = dimensionResource(R.dimen.fullscreen_loading_stroke_width)
            )

            Spacer(
                modifier = Modifier.height(dimensionResource(R.dimen.fullscreen_loading_spacing))
            )

            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FullScreenError(
    errorMessage: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier.fillMaxSize().padding(dimensionResource(R.dimen.fullscreen_loading_padding)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.error_generic),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (onRetry != null) {
                Spacer(
                    modifier =
                        Modifier.height(dimensionResource(R.dimen.fullscreen_loading_spacing))
                )

                Button(
                    onClick = onRetry,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

// Loading Components Previews
@Preview(name = "Loading Indicator - Light", showBackground = true)
@Composable
private fun LoadingIndicatorLightPreview() {
    AstroAppTheme(themePreference = 0) { LoadingIndicator() }
}

@Preview(
    name = "Loading Indicator - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LoadingIndicatorDarkPreview() {
    AstroAppTheme(themePreference = 1) { LoadingIndicator() }
}

@Preview(name = "Loading Indicator with text - Light", showBackground = true)
@Composable
private fun LoadingIndicatorWithTextLightPreview() {
    AstroAppTheme(themePreference = 0) { LoadingIndicator(text = "Loading photos...") }
}

@Preview(
    name = "Loading Indicator with text - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LoadingIndicatorWithTextDarkPreview() {
    AstroAppTheme(themePreference = 1) { LoadingIndicator(text = "Loading photos...") }
}

@Preview(name = "Error Card - Light", showBackground = true)
@Composable
private fun ErrorCardLightPreview() {
    AstroAppTheme(themePreference = 0) {
        ErrorCard(
            errorMessage = "Failed to load data. Please check your internet connection.",
            onRetry = {}
        )
    }
}

@Preview(
    name = "Error Card - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ErrorCardDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        ErrorCard(
            errorMessage = "Failed to load data. Please check your internet connection.",
            onRetry = {}
        )
    }
}

@Preview(name = "Error Card no retry - Light", showBackground = true)
@Composable
private fun ErrorCardNoRetryLightPreview() {
    AstroAppTheme(themePreference = 0) { ErrorCard(errorMessage = "An unexpected error occurred.") }
}

@Preview(
    name = "Error Card no retry - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ErrorCardNoRetryDarkPreview() {
    AstroAppTheme(themePreference = 1) { ErrorCard(errorMessage = "An unexpected error occurred.") }
}

@Preview(name = "Empty State Card - Light", showBackground = true)
@Composable
private fun EmptyStateCardLightPreview() {
    AstroAppTheme(themePreference = 0) {
        EmptyStateCard(message = "No items found", actionText = "Refresh", onAction = {})
    }
}

@Preview(
    name = "Empty State Card - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun EmptyStateCardDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        EmptyStateCard(message = "No items found", actionText = "Refresh", onAction = {})
    }
}

@Preview(name = "Empty State Card no action - Light", showBackground = true)
@Composable
private fun EmptyStateCardNoActionLightPreview() {
    AstroAppTheme(themePreference = 0) { EmptyStateCard(message = "No results match your search") }
}

@Preview(
    name = "Empty State Card no action - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun EmptyStateCardNoActionDarkPreview() {
    AstroAppTheme(themePreference = 1) { EmptyStateCard(message = "No results match your search") }
}

@Preview(name = "Full Screen Loading - Light", showBackground = true)
@Composable
private fun FullScreenLoadingLightPreview() {
    AstroAppTheme(themePreference = 0) { FullScreenLoading() }
}

@Preview(
    name = "Full Screen Loading - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FullScreenLoadingDarkPreview() {
    AstroAppTheme(themePreference = 1) { FullScreenLoading() }
}

@Preview(name = "Full Screen Error - Light", showBackground = true)
@Composable
private fun FullScreenErrorLightPreview() {
    AstroAppTheme(themePreference = 0) {
        FullScreenError(errorMessage = "Unable to connect to the server", onRetry = {})
    }
}

@Preview(
    name = "Full Screen Error - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FullScreenErrorDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        FullScreenError(errorMessage = "Unable to connect to the server", onRetry = {})
    }
}
