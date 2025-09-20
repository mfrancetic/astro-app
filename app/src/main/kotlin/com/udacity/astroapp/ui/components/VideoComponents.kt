package com.udacity.astroapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.udacity.astroapp.R
import com.udacity.astroapp.utils.VideoUtils

/** Composable for displaying YouTube video with embedded player */
@Composable
fun YouTubeVideoPlayer(
    videoUrl: String,
    title: String,
    modifier: Modifier = Modifier,
    showControls: Boolean = true,
    onFullscreenClick: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val videoId = VideoUtils.extractYouTubeVideoId(videoUrl)
    var playerState by remember { mutableStateOf(PlayerConstants.PlayerState.UNSTARTED) }
    var youTubePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }

    if (videoId != null && !VideoUtils.isUnplayableVideo(videoUrl)) {
        Box(modifier = modifier) {
            AndroidView(
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        addYouTubePlayerListener(
                            object : AbstractYouTubePlayerListener() {
                                override fun onReady(player: YouTubePlayer) {
                                    youTubePlayer = player
                                    player.cueVideo(videoId, 0f)
                                }

                                override fun onStateChange(
                                    player: YouTubePlayer,
                                    state: PlayerConstants.PlayerState
                                ) {
                                    playerState = state
                                }

                                override fun onError(
                                    player: YouTubePlayer,
                                    error: PlayerConstants.PlayerError
                                ) {
                                    onError(error.name)
                                }
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (showControls) {
                VideoControls(
                    playerState = playerState,
                    onPlayPause = {
                        youTubePlayer?.let { player ->
                            when (playerState) {
                                PlayerConstants.PlayerState.PLAYING -> player.pause()
                                PlayerConstants.PlayerState.PAUSED -> player.play()
                                else -> player.play()
                            }
                        }
                    },
                    onFullscreen = onFullscreenClick,
                    modifier =
                        Modifier.align(Alignment.BottomCenter)
                            .padding(dimensionResource(R.dimen.spacing_small))
                )
            }
        }
    } else {
        VideoErrorContent(
            errorMessage = stringResource(R.string.video_not_playable),
            modifier = modifier
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
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
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
    var youTubePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
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
                                        override fun onReady(player: YouTubePlayer) {
                                            youTubePlayer = player
                                            player.loadVideo(videoId, 0f)
                                        }

                                        override fun onStateChange(
                                            player: YouTubePlayer,
                                            state: PlayerConstants.PlayerState
                                        ) {
                                            playerState = state
                                        }

                                        override fun onError(
                                            player: YouTubePlayer,
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
                            youTubePlayer?.let { player ->
                                when (playerState) {
                                    PlayerConstants.PlayerState.PLAYING -> player.pause()
                                    PlayerConstants.PlayerState.PAUSED -> player.play()
                                    else -> player.play()
                                }
                            }
                        },
                        onFullscreen = onDismiss,
                        isFullscreen = true,
                        onVolumeToggle = {
                            youTubePlayer?.let { player ->
                                isMuted = !isMuted
                                if (isMuted) player.mute() else player.unMute()
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
