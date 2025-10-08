@file:kotlin.OptIn(ExperimentalSharedTransitionApi::class)

package net.primal.android.stream.ui

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.annotation.OptIn
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toRect
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.recyclerview.widget.RecyclerView
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
fun ExpandedLiveStreamPlayer(
    state: LiveStreamContract.UiState,
    mediaController: MediaController,
    streamUrl: String?,
    controlsVisible: Boolean,
    menuVisible: Boolean,
    isCollapsed: Boolean,
    lookaheadScope: LookaheadScope,
    onPlayPauseClick: () -> Unit,
    onClose: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSoundClick: () -> Unit,
    onControlsVisibilityChange: () -> Unit,
    onMenuVisibilityChange: (Boolean) -> Unit,
    onQuoteClick: (String) -> Unit,
    onMuteUserClick: () -> Unit,
    onUnmuteUserClick: () -> Unit,
    onReportContentClick: (ReportType) -> Unit,
    onRequestDeleteClick: () -> Unit,
    onToggleFullScreenClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    playerModifier: Modifier = Modifier,
    onReplay: () -> Unit,
    loadingModifier: Modifier = Modifier,
) {
    KeepScreenOn(enabled = state.playerState.isPlaying)

    LaunchedEffect(streamUrl) {
        val currentMediaItemUri = mediaController.currentMediaItem?.localConfiguration?.uri?.toString()

        if (currentMediaItemUri != streamUrl) {
            mediaController.stop()

            if (streamUrl != null) {
                mediaController.setMediaItem(buildMediaItem(state.naddr, streamUrl, state.streamInfo))
                mediaController.prepare()
                mediaController.playWhenReady = true
            }
        }
    }

    LaunchedEffect(state.playerState.isMuted) {
        mediaController.volume = if (state.playerState.isMuted) 0f else 1f
    }

    PlayerBox(
        modifier = modifier,
        playerModifier = playerModifier,
        loadingModifier = loadingModifier,
        state = state,
        mediaController = mediaController,
        lookaheadScope = lookaheadScope,
        controlsVisible = controlsVisible,
        menuVisible = menuVisible,
        isCollapsed = isCollapsed,
        onClose = onClose,
        onControlsVisibilityChange = onControlsVisibilityChange,
        onMenuVisibilityChange = onMenuVisibilityChange,
        onPlayPauseClick = onPlayPauseClick,
        onRewind = onRewind,
        onForward = onForward,
        onQuoteClick = onQuoteClick,
        onMuteUserClick = onMuteUserClick,
        onUnmuteUserClick = onUnmuteUserClick,
        onReportContentClick = onReportContentClick,
        onRequestDeleteClick = onRequestDeleteClick,
        onSoundClick = onSoundClick,
        onToggleFullScreenClick = onToggleFullScreenClick,
        onReplay = onReplay,
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
    lookaheadScope: LookaheadScope,
    controlsVisible: Boolean,
    menuVisible: Boolean,
    isCollapsed: Boolean,
    onClose: () -> Unit,
    onControlsVisibilityChange: () -> Unit,
    onMenuVisibilityChange: (Boolean) -> Unit,
    onPlayPauseClick: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onQuoteClick: (String) -> Unit,
    onMuteUserClick: () -> Unit,
    onUnmuteUserClick: () -> Unit,
    onReportContentClick: (ReportType) -> Unit,
    onRequestDeleteClick: () -> Unit,
    onSoundClick: () -> Unit,
    onToggleFullScreenClick: () -> Unit,
    onReplay: () -> Unit,
    onRetry: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val localConfiguration = LocalConfiguration.current
    val isLandscape = localConfiguration.orientation == ORIENTATION_LANDSCAPE

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

    val playerSizingModifier = remember(localConfiguration.orientation) {
        Modifier.resolvePlayerSizingModifier(orientation = localConfiguration.orientation)
    }

    val dragDownModifier = remember(isLandscape, isCollapsed) {
        Modifier
            .onDragDownBeyond(
                threshold = if (isCollapsed) 75.dp else 100.dp,
                onTriggered = {
                    if (isLandscape) {
                        onToggleFullScreenClick()
                    } else {
                        onClose()
                    }
                },
            )
    }

    val playerAndMessageModifier = Modifier
        .animateBounds(lookaheadScope = lookaheadScope)
        .then(dragDownModifier)
        .then(playerModifier)
        .then(playerSizingModifier)

    LiveStreamPlayerBox(
        mediaController = mediaController,
        state = state,
        modifier = modifier
            .then(boxSizingModifier)
            .background(playerBackgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onControlsVisibilityChange,
            ),
        playerModifier = playerAndMessageModifier
            .background(playerBackgroundColor)
            .then(
                Modifier.onGloballyPositioned { layoutCoordinates ->
                    pipManager.sourceRectHint = layoutCoordinates.boundsInWindow().toAndroidRectF().toRect()
                },
            ),
        loadingModifier = Modifier
            .animateBounds(lookaheadScope = lookaheadScope)
            .then(loadingModifier),
        fallbackModifier = Modifier.animateBounds(lookaheadScope = lookaheadScope),
        playerOverlay = {
            if (!isInPipMode) {
                LiveStreamPlayerControls(
                    modifier = Modifier
                        .animateBounds(lookaheadScope = lookaheadScope)
                        .fillMaxSize(),
                    isVisible = controlsVisible,
                    state = state,
                    menuVisible = menuVisible,
                    isStreamUnavailable = state.isStreamUnavailable,
                    isCollapsed = isCollapsed,
                    onMenuVisibilityChange = onMenuVisibilityChange,
                    onPlayPauseClick = onPlayPauseClick,
                    onRewind = onRewind,
                    onForward = onForward,
                    onGoToLive = { mediaController.seekToDefaultPosition() },
                    onClose = {
                        scope.launch {
                            if (isLandscape) {
                                onToggleFullScreenClick()
                                delay(100.milliseconds)
                            }
                            onClose()
                        }
                    },
                    onQuoteClick = onQuoteClick,
                    onMuteUserClick = onMuteUserClick,
                    onUnmuteUserClick = onUnmuteUserClick,
                    onReportContentClick = onReportContentClick,
                    onRequestDeleteClick = onRequestDeleteClick,
                    onSoundClick = onSoundClick,
                    onToggleFullScreenClick = onToggleFullScreenClick,
                )
            }
        },
        onReplayClick = onReplay,
        onRetryClick = onRetry,
    )
}

private fun Modifier.resolveBoxSizingModifier(@RecyclerView.Orientation orientation: Int) =
    if (orientation == ORIENTATION_LANDSCAPE) {
        this.fillMaxSize()
    } else {
        this
            .fillMaxWidth()
            .aspectRatio(VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT)
    }

private fun Modifier.resolvePlayerSizingModifier(@RecyclerView.Orientation orientation: Int): Modifier {
    return if (orientation == ORIENTATION_LANDSCAPE) {
        this
            .fillMaxHeight()
            .aspectRatio(
                ratio = VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT,
                matchHeightConstraintsFirst = true,
            )
    } else {
        Modifier.fillMaxSize()
    }
}
