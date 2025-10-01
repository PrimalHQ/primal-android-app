package net.primal.android.stream.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                        atLiveEdge = state.playerState.atLiveEdge,
                        onRewind = onRewind,
                        onPlayPauseClick = onPlayPauseClick,
                        onForward = onForward,
                    )
                }

                if (!isStreamUnavailable) {
                    PlayerActionButtons(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp),
                        isLive = state.playerState.isLive,
                        isAtLiveEdge = state.playerState.atLiveEdge,
                        isMuted = state.playerState.isMuted,
                        onGoToLive = onGoToLive,
                        onSoundClick = onSoundClick,
                        onFullscreenClick = onToggleFullScreenClick,
                    )
                }
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
    atLiveEdge: Boolean,
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
            IconButton(
                onClick = onRewind,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
            ) {
                Icon(
                    modifier = Modifier.size(42.dp),
                    imageVector = PrimalIcons.VideoBack,
                    contentDescription = stringResource(id = R.string.accessibility_rewind_15_seconds),
                )
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
            IconButton(
                onClick = onForward,
                enabled = !(isLive && atLiveEdge),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.5f),
                ),
            ) {
                Icon(
                    modifier = Modifier.size(42.dp),
                    imageVector = PrimalIcons.VideoForward,
                    contentDescription = stringResource(id = R.string.accessibility_forward_30_seconds),
                )
            }
        }
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

    val bottomPadding = if (localConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE) 40.dp else 10.dp

    Row(
        modifier = modifier.padding(bottom = bottomPadding),
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
