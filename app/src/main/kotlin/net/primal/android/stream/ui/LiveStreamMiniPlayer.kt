package net.primal.android.stream.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.VideoCloseMini
import net.primal.android.core.compose.icons.primaliconpack.VideoPauseMini
import net.primal.android.core.compose.icons.primaliconpack.VideoPlayMini
import net.primal.android.core.video.toggle
import net.primal.android.stream.LiveStreamContract
import net.primal.android.theme.AppTheme

private const val VIDEO_ASPECT_RATIO_WIDTH = 16f
private const val VIDEO_ASPECT_RATIO_HEIGHT = 9f

@UnstableApi
@Composable
fun LiveStreamMiniPlayer(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer,
    state: LiveStreamContract.UiState,
    onExpandStream: () -> Unit,
    onStopStream: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val playerWidth = this.maxWidth / 2
        var controlsOverlayVisibility by remember { mutableStateOf(false) }

        LaunchedEffect(controlsOverlayVisibility) {
            if (controlsOverlayVisibility) {
                launch {
                    delay(3.seconds)
                    controlsOverlayVisibility = false
                }
            }
        }

        Box(
            modifier = modifier
                .size(
                    width = playerWidth,
                    height = playerWidth / (VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT),
                )
                .imePadding()
                .navigationBarsPadding()
                .background(AppTheme.colorScheme.background)
                .clip(AppTheme.shapes.large)
                .clickable { controlsOverlayVisibility = true },
        ) {
            PlayerBox(exoPlayer = exoPlayer, state = state)

            AnimatedVisibility(
                visible = controlsOverlayVisibility,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                PlayerControlsRow(
                    onTogglePlayer = { exoPlayer.toggle() },
                    isPlaying = exoPlayer.isPlaying,
                    onStopStream = onStopStream,
                    onExpandStream = onExpandStream,
                )
            }
        }

    }
}

@Composable
private fun PlayerControlsRow(
    onExpandStream: () -> Unit,
    onTogglePlayer: () -> Unit,
    isPlaying: Boolean,
    onStopStream: () -> Unit,
) {
    val playPauseIcon = remember(isPlaying) {
        if (isPlaying) {
            PrimalIcons.VideoPauseMini
        } else {
            PrimalIcons.VideoPlayMini
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onExpandStream() },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onTogglePlayer) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    imageVector = playPauseIcon,
                    contentDescription = stringResource(id = R.string.accessibility_play_pause),
                )
            }
            IconButton(onClick = onStopStream) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    imageVector = PrimalIcons.VideoCloseMini,
                    contentDescription = null,
                )
            }
        }
    }
}

@UnstableApi
@Composable
private fun PlayerBox(
    exoPlayer: ExoPlayer,
    state: LiveStreamContract.UiState,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        PlayerSurface(
            modifier = Modifier.matchParentSize(),
            player = exoPlayer,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
        )

        if (state.playerState.isBuffering && !state.playerState.isPlaying) {
            PrimalLoadingSpinner()
        }
    }
}
