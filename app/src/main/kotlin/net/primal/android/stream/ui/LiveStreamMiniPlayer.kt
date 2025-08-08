package net.primal.android.stream.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import net.primal.android.R
import net.primal.android.core.compose.NavigationBarFullHeightDp
import net.primal.android.core.compose.PrimalLoadingSpinner
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
    applyBottomBarPadding: Boolean,
    state: LiveStreamContract.UiState,
    onExpandStream: () -> Unit,
    onStopStream: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = modifier
                .zIndex(1f)
                .fillMaxWidth()
                .padding(bottom = if (applyBottomBarPadding) NavigationBarFullHeightDp else 0.dp)
                .background(AppTheme.extraColorScheme.surfaceVariantAlt2)
                .padding(start = 6.dp, end = 16.dp, top = 6.dp)
                .navigationBarsPadding()
                .clickable { onExpandStream() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PlayerBox(exoPlayer = exoPlayer, state = state)
                StreamTitleAndAuthorColumn(state = state)
            }

            PlayerControlsRow(
                onTogglePlayer = { exoPlayer.toggle() },
                isPlaying = exoPlayer.isPlaying,
                onStopStream = onStopStream,
            )
        }
    }
}

@Composable
private fun PlayerControlsRow(
    onTogglePlayer: () -> Unit,
    isPlaying: Boolean,
    onStopStream: () -> Unit,
) {
    val playPauseIcon = remember(isPlaying) {
        if (isPlaying) {
            Icons.Default.Pause
        } else {
            Icons.Default.PlayArrow
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onTogglePlayer) {
            Icon(
                modifier = Modifier.size(48.dp),
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                imageVector = playPauseIcon,
                contentDescription = stringResource(id = R.string.accessibility_play_pause),
            )
        }
        IconButton(onClick = onStopStream) {
            Icon(
                modifier = Modifier.size(48.dp),
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                imageVector = Icons.Default.Close,
                contentDescription = null,
            )
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
        modifier = Modifier
            .height(64.dp)
            .aspectRatio(VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT)
            .background(Color.Black),
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

@Composable
private fun StreamTitleAndAuthorColumn(state: LiveStreamContract.UiState) {
    Column {
        state.streamInfo?.title?.let { title ->
            Text(
                text = title,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onPrimary,
                overflow = TextOverflow.Ellipsis,
            )
        }
        state.authorProfile?.authorDisplayName?.let { displayName ->
            Text(
                text = displayName,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
