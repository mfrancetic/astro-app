package com.udacity.astroapp.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.udacity.astroapp.R
import com.udacity.astroapp.ui.theme.AstroAppTheme
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun SwipeableContent(
    currentDate: LocalDate,
    maxDate: LocalDate? = null,
    onDateChanged: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Animation state for horizontal offset
    val offsetX = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Reset offset when date changes externally
    LaunchedEffect(currentDate) {
        if (!isDragging) {
            offsetX.snapTo(0f)
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(currentDate, maxDate) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            coroutineScope.launch {
                                val threshold = size.width * 0.3f
                                when {
                                    // Swiped right (previous day)
                                    offsetX.value > threshold -> {
                                        val previousDate = currentDate.minusDays(1)
                                        onDateChanged(previousDate)
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring()
                                        )
                                    }
                                    // Swiped left (next day)
                                    offsetX.value < -threshold -> {
                                        val nextDate = currentDate.plusDays(1)
                                        val isDateAllowed =
                                            maxDate == null || !nextDate.isAfter(maxDate)

                                        if (isDateAllowed) {
                                            onDateChanged(nextDate)
                                        }
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring()
                                        )
                                    }
                                    // Not enough swipe distance, snap back
                                    else -> {
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring()
                                        )
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            coroutineScope.launch {
                                offsetX.animateTo(targetValue = 0f, animationSpec = spring())
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            coroutineScope.launch {
                                val newOffset = offsetX.value + dragAmount

                                // Prevent swiping left (to future) if maxDate constraint is hit
                                val nextDate = currentDate.plusDays(1)
                                val isNextDateAllowed =
                                    maxDate == null || !nextDate.isAfter(maxDate)

                                // Allow right swipe (previous), restrict left swipe if at max date
                                when {
                                    newOffset > 0 -> {
                                        // Swiping right (previous day) - always allowed
                                        offsetX.snapTo(newOffset)
                                    }
                                    newOffset < 0 && isNextDateAllowed -> {
                                        // Swiping left (next day) - only if allowed
                                        offsetX.snapTo(newOffset)
                                    }
                                    newOffset < 0 && !isNextDateAllowed -> {
                                        // At max date, add resistance
                                        val resistance = dragAmount * 0.2f
                                        offsetX.snapTo(
                                            (offsetX.value + resistance).coerceAtMost(0f)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
    ) {
        content()
    }
}

// SwipeableContent Previews
@Preview(name = "Swipeable Content - Light", showBackground = true)
@Composable
private fun SwipeableContentLightPreview() {
    AstroAppTheme(themePreference = 0) {
        SwipeableContent(
            currentDate = LocalDate.of(2024, 1, 15),
            maxDate = LocalDate.now(),
            onDateChanged = {}
        ) {
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(dimensionResource(R.dimen.spacing_large)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Swipe left or right to change date",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

@Preview(
    name = "Swipeable Content - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SwipeableContentDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        SwipeableContent(
            currentDate = LocalDate.of(2024, 1, 15),
            maxDate = LocalDate.now(),
            onDateChanged = {}
        ) {
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(dimensionResource(R.dimen.spacing_large)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Swipe left or right to change date",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}
