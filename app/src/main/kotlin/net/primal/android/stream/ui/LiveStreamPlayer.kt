package net.primal.android.stream.ui

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toRect
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.recyclerview.widget.RecyclerView
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import net.primal.android.R
import net.primal.android.core.compose.ShadowIcon
import net.primal.android.core.compose.foundation.KeepScreenOn
import net.primal.android.core.ext.onDragDownBeyond
import net.primal.android.core.pip.LocalPiPManager
import net.primal.android.core.pip.rememberIsInPipMode
import net.primal.android.stream.LiveStreamContract
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_HEIGHT
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_WIDTH
import net.primal.android.stream.utils.buildMediaItem
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.ReportType

@OptIn(UnstableApi::class)
@Composable
fun LiveStreamPlayer(
    state: LiveStreamContract.UiState,
    mediaController: MediaController,
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
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    playerModifier: Modifier = Modifier,
    loadingModifier: Modifier = Modifier,
) {
    var controlsVisible by remember { mutableStateOf(false) }
    var menuVisible by remember { mutableStateOf(false) }

    KeepScreenOn(enabled = state.playerState.isPlaying)

    LaunchedEffect(streamUrl) {
        val currentMediaItemUri = mediaController.currentMediaItem?.localConfiguration?.uri?.toString()

        if (currentMediaItemUri != streamUrl) {
            mediaController.stop()

            mediaController.setMediaItem(buildMediaItem(state.naddr, streamUrl, state.streamInfo))
            mediaController.prepare()
            mediaController.playWhenReady = true
        }
    }

    LaunchedEffect(state.playerState.isMuted) {
        mediaController.volume = if (state.playerState.isMuted) 0f else 1f
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
        mediaController = mediaController,
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
        onRetry = onRetry,
    )
}

@Composable
@OptIn(UnstableApi::class)
@Suppress("LongMethod")
private fun PlayerBox(
    modifier: Modifier,
    playerModifier: Modifier,
    loadingModifier: Modifier,
    state: LiveStreamContract.UiState,
    mediaController: MediaController,
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
    onRetry: () -> Unit,
) {
    val localConfiguration = LocalConfiguration.current
    val isInPipMode = rememberIsInPipMode()
    val pipManager = LocalPiPManager.current

    val boxSizingModifier = remember(localConfiguration.orientation) {
        Modifier.resolveBoxSizingModifier(localConfiguration.orientation)
    }

    val playerBackgroundColor = if (
        state.isStreamUnavailable ||
        state.playerState.isVideoFinished ||
        localConfiguration.orientation == ORIENTATION_LANDSCAPE
    ) {
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

        val playerAndMessageModifier = playerModifier
            .then(playerSizingModifier)
            .onDragDownBeyond(
                threshold = 100.dp,
                onTriggered = onClose,
            )

        if (state.streamInfo?.streamUrl.isNullOrEmpty() ||
            state.isStreamUnavailable || state.playerState.isVideoFinished
        ) {
            Column(
                modifier = playerAndMessageModifier
                    .fillMaxSize()
                    .padding(top = 13.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
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

                if (state.isStreamUnavailable && !state.playerState.isVideoFinished) {
                    IconButton(onClick = onRetry) {
                        ShadowIcon(
                            modifier = Modifier.size(30.dp),
                            tint = Color.White,
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(id = R.string.live_stream_retry_button),
                        )
                    }
                }
            }
        } else {
            PlayerSurface(
                modifier = playerAndMessageModifier
                    .then(
                        Modifier.onGloballyPositioned { layoutCoordinates ->
                            pipManager.sourceRectHint = layoutCoordinates.boundsInWindow().toAndroidRectF().toRect()
                        },
                    )
                    .matchParentSize(),
                player = mediaController,
                surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            )
        }

        if (state.playerState.isLoading && !state.isStreamUnavailable) {
            StreamPlayerLoadingIndicator(modifier = loadingModifier.matchParentSize())
        }

        if (!isInPipMode) {
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
                onGoToLive = { mediaController.seekToDefaultPosition() },
                onClose = onClose,
                onSeek = { positionMs ->
                    mediaController.seekTo(positionMs)
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
