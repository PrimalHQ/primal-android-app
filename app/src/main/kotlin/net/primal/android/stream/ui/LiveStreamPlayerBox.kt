package net.primal.android.stream.ui

import android.content.res.Configuration
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import net.primal.android.R
import net.primal.android.stream.LiveStreamContract
import net.primal.android.stream.LiveStreamContract.UiEvent
import net.primal.android.theme.AppTheme

@OptIn(UnstableApi::class)
@Composable
fun LiveStreamPlayerBox(
    mediaController: MediaController,
    state: LiveStreamContract.UiState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    playerModifier: Modifier = Modifier,
    loadingModifier: Modifier = Modifier,
    fallbackModifier: Modifier = Modifier,
    playerOverlay: @Composable () -> Unit = {},
) {
    val localConfiguration = LocalConfiguration.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val showPlayerSurface = !state.playbackUrl.isNullOrEmpty() &&
            !state.isStreamUnavailable &&
            !state.playerState.isVideoFinished

        if (showPlayerSurface) {
            PlayerSurface(
                modifier = playerModifier.matchParentSize(),
                player = mediaController,
                surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            )

            playerOverlay()

            if (state.playerState.isLoading) {
                StreamPlayerLoadingIndicator(
                    modifier = loadingModifier
                        .matchParentSize()
                        .background(AppTheme.colorScheme.background),
                    isFullscreen = localConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE,
                )
            }
        } else {
            StreamFallbackContent(
                modifier = fallbackModifier.matchParentSize(),
                state = state,
                onReplayClick = {
                    mediaController.seekTo(0L)
                    mediaController.play()
                    eventPublisher(UiEvent.OnReplayStream)
                },
                onRetryClick = {
                    eventPublisher(UiEvent.OnRetryStream)
                    mediaController.prepare()
                    mediaController.play()
                },
            )
        }
    }
}

@Composable
private fun StreamFallbackContent(
    modifier: Modifier = Modifier,
    state: LiveStreamContract.UiState,
    onRetryClick: (() -> Unit)?,
    onReplayClick: (() -> Unit)?,
) {
    val hasRecording = !state.streamInfo?.recordingUrl.isNullOrEmpty()
    val isFinished = state.playerState.isVideoFinished

    val wasPlayingRecording = isFinished &&
        !state.streamInfo?.recordingUrl.isNullOrEmpty() &&
        state.playbackUrl == state.streamInfo.recordingUrl

    Column(
        modifier = modifier
            .background(AppTheme.extraColorScheme.surfaceVariantAlt1)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = if (isFinished) {
                if (wasPlayingRecording) {
                    stringResource(id = R.string.live_stream_video_ended)
                } else {
                    stringResource(id = R.string.live_stream_stream_ended)
                }
            } else {
                stringResource(id = R.string.live_stream_recording_not_available)
            }.uppercase(),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
            textAlign = TextAlign.Center,
        )

        if (isFinished && hasRecording && onReplayClick != null) {
            Button(
                modifier = Modifier.height(26.dp),
                onClick = onReplayClick,
                shape = AppTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colorScheme.onSurface,
                    contentColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.live_stream_replay_button),
                    style = AppTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                    ),
                    fontWeight = FontWeight.Bold,
                )
            }
        } else if (state.isStreamUnavailable && onRetryClick != null) {
            IconButton(onClick = onRetryClick) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(id = R.string.live_stream_retry_button),
                )
            }
        }
    }
}
