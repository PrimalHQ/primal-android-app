package net.primal.android.stream.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
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
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.5f))) {
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
                    onRewind = onRewind,
                    onPlayPauseClick = onPlayPauseClick,
                    onForward = onForward,
                )

                BottomControls(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
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
    onRewind: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onForward: () -> Unit,
) {
    Row(
        modifier = modifier,
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var localSeekPosition by remember(state.currentTime) { mutableLongStateOf(state.currentTime) }
    val sliderPosition = if (state.isSeeking) localSeekPosition else state.currentTime
    val isInteractive = state.totalDuration > 0 && (!state.isLive || !state.atLiveEdge)

    val valueRangeEnd = (state.totalDuration.takeIf { it > 0L } ?: 1L).toFloat()
    val sliderValue = if (state.isLive && state.atLiveEdge) {
        valueRangeEnd
    } else {
        sliderPosition.toFloat()
    }

    Column(modifier = modifier) {
        PlayerActionButtons(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            isLive = state.isLive,
            isAtLiveEdge = state.atLiveEdge,
            isMuted = state.isMuted,
            onGoToLive = onGoToLive,
            onSoundClick = onSoundClick,
            onFullscreenClick = onFullscreenClick,
        )

        PlayerSlider(
            modifier = Modifier.fillMaxWidth(),
            isLiveAtEdge = state.isLive && state.atLiveEdge,
            isInteractive = isInteractive,
            sliderValue = sliderValue,
            valueRangeEnd = valueRangeEnd,
            onValueChange = { newPosition ->
                if (isInteractive) {
                    if (!state.isSeeking) onSeekStarted()
                    localSeekPosition = newPosition.toLong()
                }
            },
            onValueChangeFinished = {
                if (isInteractive) {
                    onSeek(localSeekPosition)
                }
            },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerSlider(
    modifier: Modifier = Modifier,
    isLiveAtEdge: Boolean,
    isInteractive: Boolean,
    sliderValue: Float,
    valueRangeEnd: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Slider(
            modifier = Modifier
                .weight(1f)
                .height(0.dp),
            value = sliderValue,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = 0f..valueRangeEnd,
            enabled = isInteractive,
            thumb = { },
            track = { sliderState ->
                CustomTrackWithThumb(
                    sliderState = sliderState,
                    isInteractive = isInteractive,
                    isLiveAtEdge = isLiveAtEdge,
                )
            },
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTrackWithThumb(
    sliderState: SliderState,
    isInteractive: Boolean,
    isLiveAtEdge: Boolean,
) {
    val inactiveTrackColor = Color.White.copy(alpha = 0.3f)
    val activeTrackColor = AppTheme.colorScheme.primary
    val thumbColor = AppTheme.colorScheme.primary

    val activeFraction = if (sliderState.valueRange.endInclusive > sliderState.valueRange.start) {
        (
            (sliderState.value - sliderState.valueRange.start) /
                (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
            )
    } else {
        0f
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(9.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth()
                .background(color = inactiveTrackColor),
        )

        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(fraction = activeFraction)
                .background(color = if (isLiveAtEdge) thumbColor else activeTrackColor),
        )

        if (isInteractive) {
            Box(
                modifier = Modifier
                    .offset(x = maxWidth * activeFraction - 4.5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(thumbColor, CircleShape),
                )
            }
        }
    }
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
                .clip(CircleShape)
                .background(indicatorColor),
        )
        Text(
            text = stringResource(id = R.string.live_stream_live_indicator).uppercase(),
            color = textColor,
            style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
        )
    }
}
