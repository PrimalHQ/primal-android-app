package net.primal.android.stream.ui

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.recyclerview.widget.RecyclerView
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import net.primal.android.R
import net.primal.android.core.ext.onDragDownBeyond
import net.primal.android.stream.LiveStreamContract
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_HEIGHT
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_WIDTH
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.ReportType

@OptIn(UnstableApi::class)
@Composable
fun LiveStreamPlayer(
    state: LiveStreamContract.UiState,
    exoPlayer: ExoPlayer,
    streamUrl: String,
    onPlayPauseClick: () -> Unit,
    onClose: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSoundClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekStarted: () -> Unit,
    onQuoteClick: (String) -> Unit,
    onMuteUserClick: () -> Unit,
    onUnmuteUserClick: () -> Unit,
    onReportContentClick: (ReportType) -> Unit,
    onRequestDeleteClick: () -> Unit,
    onToggleFullScreenClick: () -> Unit,
    modifier: Modifier = Modifier,
    playerModifier: Modifier = Modifier,
    loadingModifier: Modifier = Modifier,
) {
    var controlsVisible by remember { mutableStateOf(false) }
    var menuVisible by remember { mutableStateOf(false) }

    LaunchedEffect(streamUrl) {
        val currentMediaItemUri = exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString()

        if (currentMediaItemUri != streamUrl) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()

            val mediaItem = MediaItem.fromUri(streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    LaunchedEffect(state.playerState.isMuted) {
        exoPlayer.volume = if (state.playerState.isMuted) 0f else 1f
    }

    LaunchedEffect(controlsVisible, menuVisible) {
        if (controlsVisible && !menuVisible) {
            delay(5.seconds)
            controlsVisible = false
        }
    }

    PlayerBox(
        modifier = modifier,
        playerModifier = playerModifier,
        loadingModifier = loadingModifier,
        state = state,
        exoPlayer = exoPlayer,
        controlsVisible = controlsVisible,
        menuVisible = menuVisible,
        onClose = onClose,
        onControlsVisibilityChange = { controlsVisible = !controlsVisible },
        onMenuVisibilityChange = { menuVisible = it },
        onPlayPauseClick = onPlayPauseClick,
        onRewind = onRewind,
        onForward = onForward,
        onSeek = onSeek,
        onSeekStarted = onSeekStarted,
        onQuoteClick = onQuoteClick,
        onMuteUserClick = onMuteUserClick,
        onUnmuteUserClick = onUnmuteUserClick,
        onReportContentClick = onReportContentClick,
        onRequestDeleteClick = onRequestDeleteClick,
        onSoundClick = onSoundClick,
        onToggleFullScreenClick = onToggleFullScreenClick,
    )
}

@Composable
@OptIn(UnstableApi::class)
private fun PlayerBox(
    modifier: Modifier,
    playerModifier: Modifier,
    loadingModifier: Modifier,
    state: LiveStreamContract.UiState,
    exoPlayer: ExoPlayer,
    controlsVisible: Boolean,
    menuVisible: Boolean,
    onClose: () -> Unit,
    onControlsVisibilityChange: () -> Unit,
    onMenuVisibilityChange: (Boolean) -> Unit,
    onPlayPauseClick: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekStarted: () -> Unit,
    onQuoteClick: (String) -> Unit,
    onMuteUserClick: () -> Unit,
    onUnmuteUserClick: () -> Unit,
    onReportContentClick: (ReportType) -> Unit,
    onRequestDeleteClick: () -> Unit,
    onSoundClick: () -> Unit,
    onToggleFullScreenClick: () -> Unit,
) {
    val localConfiguration = LocalConfiguration.current

    val boxSizingModifier = remember(localConfiguration.orientation) {
        Modifier.resolveBoxSizingModifier(localConfiguration.orientation)
    }

    val playerBackgroundColor = if (state.isStreamUnavailable || state.playerState.isVideoFinished) {
        Color.Black
    } else {
        AppTheme.colorScheme.background
    }

    Box(
        modifier = modifier
            .then(boxSizingModifier)
            .background(playerBackgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onControlsVisibilityChange,
            ),
        contentAlignment = Alignment.Center,
    ) {
        val playerSizingModifier = remember(localConfiguration.orientation) {
            Modifier.resolvePlayerSizingModifier(orientation = localConfiguration.orientation, scope = this)
        }

        if (state.isStreamUnavailable || state.playerState.isVideoFinished) {
            val messageText = if (state.playerState.isVideoFinished) {
                stringResource(id = R.string.live_stream_video_ended)
            } else {
                stringResource(id = R.string.live_stream_recording_not_available)
            }

            Text(
                text = messageText,
                style = AppTheme.typography.bodyLarge,
                color = Color.White,
            )
        } else {
            PlayerSurface(
                modifier = playerModifier
                    .then(playerSizingModifier)
                    .onDragDownBeyond(
                        threshold = 100.dp,
                        onTriggered = onClose,
                    )
                    .matchParentSize(),
                player = exoPlayer,
                surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            )
        }

        if (state.playerState.isLoading && !state.isStreamUnavailable) {
            StreamPlayerLoadingIndicator(modifier = loadingModifier.matchParentSize())
        }

        LiveStreamPlayerControls(
            modifier = Modifier.fillMaxSize(),
            isVisible = controlsVisible,
            state = state,
            menuVisible = menuVisible,
            isStreamUnavailable = state.isStreamUnavailable,
            onMenuVisibilityChange = onMenuVisibilityChange,
            onPlayPauseClick = onPlayPauseClick,
            onRewind = onRewind,
            onForward = onForward,
            onGoToLive = { exoPlayer.seekToDefaultPosition() },
            onClose = onClose,
            onSeek = { positionMs ->
                exoPlayer.seekTo(positionMs)
                onSeek(positionMs)
            },
            onSeekStarted = onSeekStarted,
            onQuoteClick = onQuoteClick,
            onMuteUserClick = onMuteUserClick,
            onUnmuteUserClick = onUnmuteUserClick,
            onReportContentClick = onReportContentClick,
            onRequestDeleteClick = onRequestDeleteClick,
            onSoundClick = onSoundClick,
            onToggleFullScreenClick = onToggleFullScreenClick,
        )
    }
}

private fun Modifier.resolveBoxSizingModifier(@RecyclerView.Orientation orientation: Int) =
    if (orientation == ORIENTATION_LANDSCAPE) {
        this.fillMaxSize()
    } else {
        this
            .fillMaxWidth()
            .aspectRatio(VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT)
    }

private fun Modifier.resolvePlayerSizingModifier(
    @RecyclerView.Orientation orientation: Int,
    scope: BoxScope,
): Modifier {
    return if (orientation == ORIENTATION_LANDSCAPE) {
        this
            .fillMaxHeight()
            .aspectRatio(
                ratio = VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT,
                matchHeightConstraintsFirst = true,
            )
    } else {
        with(scope) { this@resolvePlayerSizingModifier.matchParentSize() }
    }
}
