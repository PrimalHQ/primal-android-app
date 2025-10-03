package net.primal.android.stream.ui

import android.content.res.Configuration
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import net.primal.android.R
import net.primal.android.core.compose.ShadowIcon
import net.primal.android.stream.LiveStreamContract
import net.primal.android.theme.AppTheme

@OptIn(UnstableApi::class)
@Composable
fun LiveStreamPlayerBox(
    mediaController: MediaController,
    state: LiveStreamContract.UiState,
    modifier: Modifier = Modifier,
    playerModifier: Modifier = Modifier,
    loadingModifier: Modifier = Modifier,
    playerOverlay: @Composable () -> Unit = {},
    onRetryClick: (() -> Unit)? = null,
) {
    val localConfiguration = LocalConfiguration.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val showPlayerSurface = !state.streamInfo?.streamUrl.isNullOrEmpty() &&
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
                        .background(Color.Black),
                    isFullscreen = localConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE,
                )
            }
        } else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(4.dp),
                ) {
                    val messageText = if (state.playerState.isVideoFinished) {
                        stringResource(id = R.string.live_stream_video_ended)
                    } else {
                        stringResource(id = R.string.live_stream_recording_not_available)
                    }

                    Text(
                        text = messageText,
                        color = Color.White,
                        style = AppTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )

                    onRetryClick?.let {
                        if (state.isStreamUnavailable && !state.playerState.isVideoFinished) {
                            IconButton(onClick = onRetryClick) {
                                ShadowIcon(
                                    modifier = Modifier.size(30.dp),
                                    tint = Color.White,
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(id = R.string.live_stream_retry_button),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
