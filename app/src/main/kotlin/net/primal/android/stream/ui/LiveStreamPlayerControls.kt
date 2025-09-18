package net.primal.android.stream.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import java.util.Locale
import java.util.concurrent.TimeUnit
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FullScreen
import net.primal.android.core.compose.icons.primaliconpack.FullScreenRestore
import net.primal.android.core.compose.icons.primaliconpack.Minimize
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.compose.icons.primaliconpack.SoundOff
import net.primal.android.core.compose.icons.primaliconpack.SoundOn
import net.primal.android.core.compose.icons.primaliconpack.VideoBack
import net.primal.android.core.compose.icons.primaliconpack.VideoForward
import net.primal.android.core.compose.icons.primaliconpack.VideoPause
import net.primal.android.core.compose.icons.primaliconpack.VideoPlay
import net.primal.android.stream.LiveStreamContract
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.ReportType

private const val SECONDS_IN_A_MINUTE = 60
private val ControlsVerticalOverlap = (-20).dp

@Composable
fun LiveStreamPlayerControls(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    state: LiveStreamContract.UiState,
    menuVisible: Boolean,
    isStreamUnavailable: Boolean,
    onMenuVisibilityChange: (Boolean) -> Unit,
    onPlayPauseClick: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSoundClick: () -> Unit,
    onGoToLive: () -> Unit,
    onClose: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekStarted: () -> Unit,
    onQuoteClick: (String) -> Unit,
    onMuteUserClick: () -> Unit,
    onUnmuteUserClick: () -> Unit,
    onReportContentClick: (ReportType) -> Unit,
    onRequestDeleteClick: () -> Unit,
    onToggleFullScreenClick: () -> Unit,
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
            ) {
                TopPlayerControls(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp),
                    state = state,
                    menuVisible = menuVisible,
                    onMenuVisibilityChange = onMenuVisibilityChange,
                    onClose = onClose,
                    onQuoteClick = onQuoteClick,
                    onMuteUserClick = onMuteUserClick,
                    onUnmuteUserClick = onUnmuteUserClick,
                    onReportContentClick = onReportContentClick,
                    onRequestDeleteClick = onRequestDeleteClick,
                )

                if (!isStreamUnavailable) {
                    CenterPlayerControls(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        isPlaying = state.playerState.isPlaying,
                        isLive = state.playerState.isLive,
                        isBuffering = state.playerState.isBuffering,
                        onRewind = onRewind,
                        onPlayPauseClick = onPlayPauseClick,
                        onForward = onForward,
                    )
                }
            }
        }

        if (isVisible && !isStreamUnavailable) {
            Popup(
                alignment = Alignment.BottomCenter,
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    clippingEnabled = false,
                ),
            ) {
                BottomControls(
                    modifier = Modifier.fillMaxWidth(),
                    state = state.playerState,
                    onSeek = onSeek,
                    onGoToLive = onGoToLive,
                    onSeekStarted = onSeekStarted,
                    onSoundClick = onSoundClick,
                    onFullscreenClick = onToggleFullScreenClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopPlayerControls(
    modifier: Modifier,
    state: LiveStreamContract.UiState,
    menuVisible: Boolean,
    onMenuVisibilityChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    onQuoteClick: (String) -> Unit,
    onMuteUserClick: () -> Unit,
    onUnmuteUserClick: () -> Unit,
    onReportContentClick: (ReportType) -> Unit,
    onRequestDeleteClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppBarIcon(icon = PrimalIcons.Minimize, onClick = onClose, tint = Color.White)

        val naddr = state.naddr
        val streamInfo = state.streamInfo
        val mainHostId = streamInfo?.mainHostId
        if (streamInfo != null && mainHostId != null && naddr != null) {
            LiveStreamMenu(
                modifier = Modifier,
                naddr = naddr,
                isMainHostMuted = state.activeUserMutedProfiles.contains(state.streamInfo.mainHostId),
                isActiveUserMainHost = state.activeUserId == mainHostId,
                rawNostrEvent = streamInfo.rawNostrEventJson,
                menuVisible = menuVisible,
                onMenuVisibilityChange = onMenuVisibilityChange,
                onQuoteClick = onQuoteClick,
                onMuteUserClick = onMuteUserClick,
                onUnmuteUserClick = onUnmuteUserClick,
                onReportContentClick = onReportContentClick,
                onRequestDeleteClick = onRequestDeleteClick,
                primalName = streamInfo.mainHostProfile?.primalName,
            ) {
                Icon(
                    imageVector = PrimalIcons.More,
                    contentDescription = "More options",
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp),
                )
            }
        } else {
            AppBarIcon(icon = PrimalIcons.More, onClick = {})
        }
    }
}

@Composable
private fun CenterPlayerControls(
    modifier: Modifier,
    isPlaying: Boolean,
    isLive: Boolean,
    isBuffering: Boolean,
    onRewind: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onForward: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = !isBuffering,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!isLive) {
                IconButton(
                    onClick = onRewind,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                ) {
                    Icon(
                        modifier = Modifier.size(42.dp),
                        imageVector = PrimalIcons.VideoBack,
                        contentDescription = stringResource(id = R.string.accessibility_rewind_10_seconds),
                    )
                }
            }
            IconButton(
                onClick = onPlayPauseClick,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = if (isPlaying) PrimalIcons.VideoPause else PrimalIcons.VideoPlay,
                    contentDescription = stringResource(id = R.string.accessibility_play_pause),
                )
            }
            if (!isLive) {
                IconButton(
                    onClick = onForward,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                ) {
                    Icon(
                        modifier = Modifier.size(42.dp),
                        imageVector = PrimalIcons.VideoForward,
                        contentDescription = stringResource(id = R.string.accessibility_forward_10_seconds),
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomControls(
    modifier: Modifier = Modifier,
    state: LiveStreamContract.PlayerState,
    onSeek: (Long) -> Unit,
    onGoToLive: () -> Unit,
    onSeekStarted: () -> Unit,
    onSoundClick: () -> Unit,
    onFullscreenClick: () -> Unit,
) {
    val isInteractive = state.totalDuration > 0 && (!state.isLive || !state.atLiveEdge)
    val totalDuration = state.totalDuration.takeIf { it > 0L } ?: 1L

    val progress = if (state.isLive && state.atLiveEdge) {
        1f
    } else {
        (state.currentTime.toFloat() / totalDuration).coerceIn(0f, 1f)
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val bottomPadding = if (isLandscape) 20.dp else 0.dp

    Column(
        modifier = modifier.padding(
            start = bottomPadding,
            bottom = bottomPadding,
            end = bottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(ControlsVerticalOverlap),
    ) {
        PlayerActionButtons(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            isLive = state.isLive,
            isAtLiveEdge = state.atLiveEdge,
            isMuted = state.isMuted,
            onGoToLive = onGoToLive,
            onSoundClick = onSoundClick,
            onFullscreenClick = onFullscreenClick,
        )

        PrimalSeekBar(
            progress = progress,
            isInteractive = isInteractive,
            onScrub = { newProgress ->
                if (isInteractive) {
                    if (!state.isSeeking) onSeekStarted()
                    val newPosition = (newProgress * totalDuration).toLong()
                    onSeek(newPosition)
                }
            },
            onScrubEnd = {
                if (state.isSeeking) {
                    val finalPosition = (progress * totalDuration).toLong()
                    onSeek(finalPosition)
                }
            },
            totalDurationMs = state.totalDuration,
            currentTimeMs = state.currentTime,
        )
    }
}

@Composable
private fun PlayerActionButtons(
    modifier: Modifier = Modifier,
    isLive: Boolean,
    isAtLiveEdge: Boolean,
    isMuted: Boolean,
    onGoToLive: () -> Unit,
    onSoundClick: () -> Unit,
    onFullscreenClick: () -> Unit,
) {
    val localConfiguration = LocalConfiguration.current
    val fullScreenIcon = remember(localConfiguration.orientation) {
        if (localConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            PrimalIcons.FullScreenRestore
        } else {
            PrimalIcons.FullScreen
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiveIndicator(
            modifier = Modifier.clickable(
                enabled = !isAtLiveEdge,
                onClick = onGoToLive,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ),
            isAtLiveEdge = isLive && isAtLiveEdge,
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onSoundClick) {
            Icon(
                imageVector = if (isMuted) PrimalIcons.SoundOff else PrimalIcons.SoundOn,
                contentDescription = "Sound",
                tint = Color.White,
            )
        }
        IconButton(onClick = onFullscreenClick) {
            Icon(
                imageVector = fullScreenIcon,
                contentDescription = "Full screen",
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun PrimalSeekBar(
    progress: Float,
    isInteractive: Boolean,
    onScrub: (Float) -> Unit,
    onScrubEnd: () -> Unit,
    modifier: Modifier = Modifier,
    totalDurationMs: Long,
    currentTimeMs: Long,
) {
    val accessibleDescription = stringResource(
        id = R.string.accessibility_live_stream_seek_bar,
        formatDuration(currentTimeMs),
        formatDuration(totalDurationMs),
    )

    var isDragging by remember { mutableStateOf(false) }
    var visualTarget by remember { mutableFloatStateOf(progress) }

    LaunchedEffect(progress, isDragging) {
        if (!isDragging) {
            visualTarget = progress
        }
    }

    val visualProgress by animateFloatAsState(
        targetValue = visualTarget,
        animationSpec = if (isDragging) snap() else spring(),
        label = "SeekBarVisualProgress",
    )

    val touchableAreaHeight = 32.dp

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibleDescription
                if (isInteractive) {
                    setProgress { target ->
                        visualTarget = target
                        onScrub(target)
                        onScrubEnd()
                        true
                    }
                }
            }
            .pointerInput(isInteractive) {
                if (!isInteractive) return@pointerInput
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    isDragging = false
                    visualTarget = newProgress
                    onScrub(newProgress)
                    onScrubEnd()
                }
            }
            .pointerInput(isInteractive) {
                if (!isInteractive) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        visualTarget = newProgress
                        onScrub(newProgress)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        visualTarget = newProgress
                        onScrub(newProgress)
                    },
                    onDragEnd = {
                        isDragging = false
                        onScrubEnd()
                    },
                    onDragCancel = {
                        isDragging = false
                        onScrubEnd()
                    },
                )
            }
            .fillMaxWidth()
            .height(touchableAreaHeight),
        contentAlignment = Alignment.BottomCenter,
    ) {
        SeekBarTrack(
            visualProgress = visualProgress,
            isInteractive = isInteractive,
        )
    }
}

@Composable
private fun SeekBarTrack(
    modifier: Modifier = Modifier,
    visualProgress: Float,
    isInteractive: Boolean,
) {
    val inactiveTrackColor = Color.White.copy(alpha = 0.3f)
    val activeTrackColor = AppTheme.colorScheme.primary
    val thumbColor = AppTheme.colorScheme.primary
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val trackHeight = 3.dp
    val thumbRadius = 8.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight),
    ) {
        val trackY = center.y
        val trackStroke = trackHeight.toPx()
        val canvasWidth = size.width

        drawLine(
            color = inactiveTrackColor,
            start = Offset(0f, trackY),
            end = Offset(canvasWidth, trackY),
            strokeWidth = trackStroke,
            cap = StrokeCap.Round,
        )

        val progressPx = (visualProgress * canvasWidth).coerceIn(0f, canvasWidth)
        val (startPx, endPx) = if (isRtl) {
            Pair(canvasWidth, canvasWidth - progressPx)
        } else {
            Pair(0f, progressPx)
        }
        drawLine(
            color = activeTrackColor,
            start = Offset(startPx, trackY),
            end = Offset(endPx, trackY),
            strokeWidth = trackStroke,
            cap = StrokeCap.Round,
        )

        if (isInteractive) {
            val thumbX = if (isRtl) canvasWidth - progressPx else progressPx
            drawCircle(
                color = thumbColor,
                radius = thumbRadius.toPx(),
                center = Offset(thumbX, trackY),
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % SECONDS_IN_A_MINUTE
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}

@Composable
private fun LiveIndicator(modifier: Modifier = Modifier, isAtLiveEdge: Boolean) {
    val indicatorColor = if (isAtLiveEdge) Color.Red else Color.Gray
    val textColor = if (isAtLiveEdge) Color.White else Color.Gray
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(size = 8.dp)
                .background(indicatorColor, shape = AppTheme.shapes.extraLarge),
        )
        Text(
            text = stringResource(id = R.string.live_stream_live_indicator).uppercase(),
            color = textColor,
            style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
        )
    }
}
