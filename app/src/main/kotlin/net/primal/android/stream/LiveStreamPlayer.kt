package net.primal.android.stream

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import net.primal.android.core.compose.PrimalLoadingSpinner

private const val VIDEO_ASPECT_RATIO_WIDTH = 16f
private const val VIDEO_ASPECT_RATIO_HEIGHT = 9f

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun LiveStreamPlayer(
    exoPlayer: ExoPlayer,
    streamUrl: String,
    playerState: LiveStreamContract.PlayerState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    onPlayPauseClick: () -> Unit,
    onClose: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var controlsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(streamUrl) {
        val mediaItem = MediaItem.fromUri(streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(5.seconds)
            controlsVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT)
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { controlsVisible = !controlsVisible },
            ),
        contentAlignment = Alignment.Center,
    ) {
        PlayerSurface(
            modifier = Modifier.matchParentSize(),
            player = exoPlayer,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
        )

        if (playerState.isBuffering && !playerState.isPlaying) {
            PrimalLoadingSpinner()
        }

        LiveStreamPlayerControls(
            modifier = Modifier.matchParentSize(),
            isVisible = controlsVisible,
            playerState = playerState,
            onPlayPauseClick = onPlayPauseClick,
            onRewind = onRewind,
            onForward = onForward,
            onGoToLive = { exoPlayer.seekToDefaultPosition() },
            onClose = onClose,
            onSeek = { positionMs ->
                exoPlayer.seekTo(positionMs)
                eventPublisher(LiveStreamContract.UiEvent.OnSeek(positionMs = positionMs))
            },
            onSeekStarted = {
                eventPublisher(LiveStreamContract.UiEvent.OnSeekStarted)
            },
        )
    }
}
