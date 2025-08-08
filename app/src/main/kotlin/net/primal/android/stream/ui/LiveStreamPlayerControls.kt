package net.primal.android.stream.ui

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import java.util.concurrent.TimeUnit
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.stream.LiveStreamContract
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.ReportType

private const val SECONDS_IN_MINUTE = 60

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamPlayerControls(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    state: LiveStreamContract.UiState,
    onPlayPauseClick: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onGoToLive: () -> Unit,
    onClose: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekStarted: () -> Unit,
    onQuoteClick: (String) -> Unit,
    onMuteUserClick: () -> Unit,
    onUnmuteUserClick: () -> Unit,
    onReportContentClick: (ReportType) -> Unit,
    onRequestDeleteClick: () -> Unit,
    onBookmarkClick: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.6f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.6f),
                    ),
                ),
            ),
        ) {
            TopPlayerControls(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 8.dp),
                state = state,
                onClose = onClose,
                onQuoteClick = onQuoteClick,
                onMuteUserClick = onMuteUserClick,
                onUnmuteUserClick = onUnmuteUserClick,
                onReportContentClick = onReportContentClick,
                onRequestDeleteClick = onRequestDeleteClick,
                onBookmarkClick = onBookmarkClick,
            )

            CenterPlayerControls(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                isPlaying = state.playerState.isPlaying,
                onRewind = onRewind,
                onPlayPauseClick = onPlayPauseClick,
                onForward = onForward,
            )

            BottomControls(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                state = state.playerState,
                onSeek = onSeek,
                onGoToLive = onGoToLive,
                onSeekStarted = onSeekStarted,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopPlayerControls(
    modifier: Modifier,
    state: LiveStreamContract.UiState,
    onClose: () -> Unit,
    onQuoteClick: (String) -> Unit,
    onMuteUserClick: () -> Unit,
    onUnmuteUserClick: () -> Unit,
    onReportContentClick: (ReportType) -> Unit,
    onRequestDeleteClick: () -> Unit,
    onBookmarkClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppBarIcon(icon = PrimalIcons.ArrowBack, onClick = onClose)

        val streamInfo = state.streamInfo
        val authorId = state.profileId
        val naddr = state.naddr
        if (streamInfo != null && authorId != null && naddr != null) {
            LiveStreamMenu(
                modifier = Modifier,
                naddr = naddr,
                isMuted = state.isMuted,
                isBookmarked = state.isBookmarked,
                isStreamAuthor = state.activeUserId == authorId,
                rawNostrEvent = streamInfo.rawNostrEventJson,
                onQuoteClick = onQuoteClick,
                onMuteUserClick = onMuteUserClick,
                onUnmuteUserClick = onUnmuteUserClick,
                onReportContentClick = onReportContentClick,
                onRequestDeleteClick = onRequestDeleteClick,
                onBookmarkClick = onBookmarkClick,
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
    onRewind: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onForward: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onRewind,
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
        ) {
            Icon(
                modifier = Modifier.size(42.dp),
                imageVector = Icons.Default.Replay10,
                contentDescription = stringResource(id = R.string.accessibility_rewind_10_seconds),
            )
        }
        IconButton(
            onClick = onPlayPauseClick,
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
        ) {
            Icon(
                modifier = Modifier.size(64.dp),
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = stringResource(id = R.string.accessibility_play_pause),
            )
        }
        IconButton(
            onClick = onForward,
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
        ) {
            Icon(
                modifier = Modifier.size(42.dp),
                imageVector = Icons.Default.Forward10,
                contentDescription = stringResource(id = R.string.accessibility_forward_10_seconds),
            )
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
) {
    var localSeekPosition by remember(state.currentTime) { mutableLongStateOf(state.currentTime) }

    val sliderPosition = if (state.isSeeking) localSeekPosition else state.currentTime
    val isInteractive = state.totalDuration > 0 && (!state.isLive || !state.atLiveEdge)

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (state.isLive) {
            LiveIndicator(
                modifier = Modifier.clickable(
                    enabled = !state.atLiveEdge,
                    onClick = onGoToLive,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ),
                isAtLiveEdge = state.atLiveEdge,
            )
        }

        val valueRangeEnd = (state.totalDuration.takeIf { it > 0L } ?: 1L).toFloat()
        val sliderValue = if (state.isLive && state.atLiveEdge) {
            valueRangeEnd
        } else {
            sliderPosition.toFloat()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!state.isLive) {
                Text(
                    text = formatDuration(sliderPosition),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.width(52.dp),
                )
            }

            Slider(
                modifier = Modifier.weight(1f),
                value = sliderValue,
                onValueChange = { newPosition ->
                    if (isInteractive) {
                        if (!state.isSeeking) {
                            onSeekStarted()
                        }
                        localSeekPosition = newPosition.toLong()
                    }
                },
                onValueChangeFinished = {
                    if (isInteractive) {
                        onSeek(localSeekPosition)
                    }
                },
                valueRange = 0f..valueRangeEnd,
                enabled = isInteractive,
                thumb = { },
                track = { sliderState ->
                    CustomTrackWithThumb(
                        sliderState = sliderState,
                        isInteractive = isInteractive,
                        isLiveAtEdge = state.isLive && state.atLiveEdge,
                    )
                },
            )

            if (!state.isLive) {
                Text(
                    text = formatDuration(state.totalDuration),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.width(52.dp),
                )
            }
        }
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
                .background(inactiveTrackColor, RoundedCornerShape(1.dp)),
        )

        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(fraction = activeFraction)
                .background(if (isLiveAtEdge) thumbColor else activeTrackColor, RoundedCornerShape(1.dp)),
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

private fun formatDuration(millis: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis)
    val minutes = totalSeconds / SECONDS_IN_MINUTE
    val seconds = totalSeconds % SECONDS_IN_MINUTE
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
