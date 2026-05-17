package com.udacity.astroapp.ui.components

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.udacity.astroapp.R
import com.udacity.astroapp.ui.theme.AstroAppTheme
import com.udacity.astroapp.utils.VideoUtils

/** Composable for displaying YouTube video with embedded player */
@Composable
fun YouTubeVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    onFullscreenClick: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val videoId = VideoUtils.extractYouTubeVideoId(videoUrl)
    var youTubePlayerView by remember { mutableStateOf<YouTubePlayerView?>(null) }
    var playbackPosition by rememberSaveable { mutableFloatStateOf(0f) }
    var player by remember { mutableStateOf<YouTubePlayer?>(null) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val orientation = remember { mutableIntStateOf(context.resources.configuration.orientation) }

    LaunchedEffect(configuration) { orientation.intValue = configuration.orientation }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> player?.pause()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            youTubePlayerView?.release()
        }
    }

    if (videoId != null) {
        Box(contentAlignment = Alignment.Center, modifier = modifier.background(Color.Black)) {
            AndroidView(
                modifier = Modifier.align(Alignment.Center),
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        youTubePlayerView = this
                        lifecycleOwner.lifecycle.addObserver(this)

                        addYouTubePlayerListener(
                            object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    player = youTubePlayer

                                    try {
                                        youTubePlayer.loadVideo(videoId, playbackPosition)
                                    } catch (e: Exception) {
                                        Log.e("YouTubePlayer", "Failed to load video", e)
                                        onError("Failed to load video: ${e.message}")
                                    }
                                }

                                override fun onCurrentSecond(
                                    youTubePlayer: YouTubePlayer,
                                    second: Float
                                ) {
                                    playbackPosition = second
                                }

                                override fun onError(
                                    youTubePlayer: YouTubePlayer,
                                    error: PlayerConstants.PlayerError
                                ) {
                                    Log.e("YouTubePlayer", "Player error: ${error.name}")

                                    if (
                                        error ==
                                            PlayerConstants.PlayerError
                                                .VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER ||
                                            error == PlayerConstants.PlayerError.UNKNOWN
                                    ) {
                                        Log.w(
                                            "YouTubePlayer",
                                            "Video not playable in embedded player: $videoId"
                                        )
                                        // Let the native YouTube error screen handle it — it
                                        // already has a Watch on YouTube button
                                    } else {
                                        onError("YouTube Player Error: ${error.name}")
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    } else {
        VideoErrorContent(
            errorMessage = stringResource(R.string.video_not_playable),
            modifier = modifier
        )
    }
}

/** Composable for playing direct video URLs (e.g. mp4/webm) via ExoPlayer */
@Composable
fun DirectVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(modifier) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize().align(Alignment.Center)
        )
    }
}

/** Video control overlay with play/pause and fullscreen buttons */
@Composable
fun VideoControls(
    playerState: PlayerConstants.PlayerState,
    onPlayPause: () -> Unit,
    onFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    onVolumeToggle: () -> Unit = {},
    isMuted: Boolean = false
) {
    Row(
        modifier =
            modifier
                .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
                .padding(dimensionResource(R.dimen.spacing_small)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
    ) {
        // Play/Pause button
        IconButton(
            onClick = onPlayPause,
            modifier =
                Modifier.size(dimensionResource(R.dimen.video_control_button_size))
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
        ) {
            Icon(
                imageVector =
                    if (playerState == PlayerConstants.PlayerState.PLAYING) Icons.Default.Pause
                    else Icons.Default.PlayArrow,
                contentDescription =
                    if (playerState == PlayerConstants.PlayerState.PLAYING)
                        stringResource(R.string.pause_video)
                    else stringResource(R.string.play_video_button),
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Volume button (optional)
        IconButton(
            onClick = onVolumeToggle,
            modifier =
                Modifier.size(dimensionResource(R.dimen.video_control_button_size))
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
        ) {
            Icon(
                imageVector =
                    if (isMuted) Icons.AutoMirrored.Filled.VolumeOff
                    else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription =
                    if (isMuted) stringResource(R.string.unmute_video)
                    else stringResource(R.string.mute_video),
                tint = Color.Black
            )
        }

        // Fullscreen button
        IconButton(
            onClick = onFullscreen,
            modifier =
                Modifier.size(dimensionResource(R.dimen.video_control_button_size))
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
        ) {
            Icon(
                imageVector =
                    if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription =
                    if (isFullscreen) stringResource(R.string.exit_fullscreen)
                    else stringResource(R.string.enter_fullscreen),
                tint = Color.Black
            )
        }
    }
}

/** Error state for unplayable videos */
@Composable
fun VideoErrorContent(errorMessage: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.video_error_icon_size)),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/** Fullscreen video dialog */
@Composable
fun FullscreenVideoDialog(videoUrl: String, onDismiss: () -> Unit) {
    val videoId = VideoUtils.extractYouTubeVideoId(videoUrl)
    var playerState by remember { mutableStateOf(PlayerConstants.PlayerState.UNSTARTED) }
    var player by remember { mutableStateOf<YouTubePlayer?>(null) }
    var isMuted by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            if (videoId != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { context ->
                            YouTubePlayerView(context).apply {
                                addYouTubePlayerListener(
                                    object : AbstractYouTubePlayerListener() {
                                        override fun onReady(youTubePlayer: YouTubePlayer) {
                                            player = youTubePlayer
                                            youTubePlayer.loadVideo(videoId, 0f)
                                        }

                                        override fun onStateChange(
                                            youTubePlayer: YouTubePlayer,
                                            state: PlayerConstants.PlayerState
                                        ) {
                                            playerState = state
                                        }

                                        override fun onError(
                                            youTubePlayer: YouTubePlayer,
                                            error: PlayerConstants.PlayerError
                                        ) {
                                            onDismiss()
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    VideoControls(
                        playerState = playerState,
                        onPlayPause = {
                            player?.let { youTubePlayer ->
                                try {
                                    when (playerState) {
                                        PlayerConstants.PlayerState.PLAYING -> youTubePlayer.pause()
                                        PlayerConstants.PlayerState.PAUSED -> youTubePlayer.play()
                                        PlayerConstants.PlayerState.ENDED ->
                                            youTubePlayer.seekTo(0f)
                                        else -> youTubePlayer.play()
                                    }
                                } catch (e: Exception) {
                                    Log.e(
                                        "YouTubeVideoPlayer",
                                        "Error while playing the video with ID $videoId",
                                        e
                                    )
                                    onDismiss()
                                }
                            }
                        },
                        onFullscreen = onDismiss,
                        isFullscreen = true,
                        onVolumeToggle = {
                            player?.let { youTubePlayer ->
                                try {
                                    isMuted = !isMuted
                                    if (isMuted) youTubePlayer.mute() else youTubePlayer.unMute()
                                } catch (e: Exception) {
                                    Log.e(
                                        "YouTubeVideoPlayer",
                                        "Error while changing the mute status of the video with ID $videoId",
                                        e
                                    )
                                    isMuted = !isMuted
                                }
                            }
                        },
                        isMuted = isMuted,
                        modifier =
                            Modifier.align(Alignment.BottomCenter)
                                .padding(dimensionResource(R.dimen.spacing_medium))
                    )
                }
            }
        }
    }
}

// Video Components Previews
@Preview(name = "Video Controls Playing - Light", showBackground = true)
@Composable
private fun VideoControlsPlayingLightPreview() {
    AstroAppTheme(themePreference = 0) {
        VideoControls(
            playerState = PlayerConstants.PlayerState.PLAYING,
            onPlayPause = {},
            onFullscreen = {},
            isFullscreen = false,
            onVolumeToggle = {},
            isMuted = false
        )
    }
}

@Preview(
    name = "Video Controls Playing - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun VideoControlsPlayingDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        VideoControls(
            playerState = PlayerConstants.PlayerState.PLAYING,
            onPlayPause = {},
            onFullscreen = {},
            isFullscreen = false,
            onVolumeToggle = {},
            isMuted = false
        )
    }
}

@Preview(name = "Video Controls Paused - Light", showBackground = true)
@Composable
private fun VideoControlsPausedLightPreview() {
    AstroAppTheme(themePreference = 0) {
        VideoControls(
            playerState = PlayerConstants.PlayerState.PAUSED,
            onPlayPause = {},
            onFullscreen = {},
            isFullscreen = false,
            onVolumeToggle = {},
            isMuted = true
        )
    }
}

@Preview(
    name = "Video Controls Paused - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun VideoControlsPausedDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        VideoControls(
            playerState = PlayerConstants.PlayerState.PAUSED,
            onPlayPause = {},
            onFullscreen = {},
            isFullscreen = false,
            onVolumeToggle = {},
            isMuted = true
        )
    }
}

@Preview(name = "Video Controls Fullscreen - Light", showBackground = true)
@Composable
private fun VideoControlsFullscreenLightPreview() {
    AstroAppTheme(themePreference = 0) {
        VideoControls(
            playerState = PlayerConstants.PlayerState.PLAYING,
            onPlayPause = {},
            onFullscreen = {},
            isFullscreen = true,
            onVolumeToggle = {},
            isMuted = false
        )
    }
}

@Preview(
    name = "Video Controls Fullscreen - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun VideoControlsFullscreenDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        VideoControls(
            playerState = PlayerConstants.PlayerState.PLAYING,
            onPlayPause = {},
            onFullscreen = {},
            isFullscreen = true,
            onVolumeToggle = {},
            isMuted = false
        )
    }
}

@Preview(name = "Video Error Content - Light", showBackground = true)
@Composable
private fun VideoErrorContentLightPreview() {
    AstroAppTheme(themePreference = 0) {
        VideoErrorContent(
            errorMessage = "This video cannot be played",
            modifier =
                Modifier.fillMaxSize().height(dimensionResource(R.dimen.photo_content_height))
        )
    }
}

@Preview(
    name = "Video Error Content - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun VideoErrorContentDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        VideoErrorContent(
            errorMessage = "This video cannot be played",
            modifier =
                Modifier.fillMaxSize().height(dimensionResource(R.dimen.photo_content_height))
        )
    }
}
