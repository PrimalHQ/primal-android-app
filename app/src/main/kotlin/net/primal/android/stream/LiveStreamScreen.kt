package net.primal.android.stream

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.profile.approvals.FollowsApprovalAlertDialog
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.theme.AppTheme

private const val LIVE_EDGE_THRESHOLD_MS = 20_000
private const val PLAYER_STATE_UPDATE_INTERVAL_MS = 200L
private const val SEEK_INCREMENT_MS = 10_000L

@Composable
fun LiveStreamScreen(viewModel: LiveStreamViewModel, onClose: () -> Unit) {
    val uiState by viewModel.state.collectAsState()

    LiveStreamScreen(
        state = uiState,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveStreamScreen(
    state: LiveStreamContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val snackbarHostState = remember { SnackbarHostState() }

    if (state.shouldApproveProfileAction != null) {
        FollowsApprovalAlertDialog(
            followsApproval = state.shouldApproveProfileAction,
            onFollowsActionsApproved = {
                eventPublisher(LiveStreamContract.UiEvent.ApproveFollowsActions(it.actions))
            },
            onClose = { eventPublisher(LiveStreamContract.UiEvent.DismissConfirmFollowUnfollowAlertDialog) },
        )
    }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { eventPublisher(LiveStreamContract.UiEvent.DismissError) },
    )

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                eventPublisher(LiveStreamContract.UiEvent.OnPlayerStateUpdate(isPlaying = isPlaying))
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                eventPublisher(
                    LiveStreamContract.UiEvent.OnPlayerStateUpdate(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                    ),
                )
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(state.isPlaying, state.isLive, state.isSeeking) {
        while (true) {
            if (!state.isSeeking) {
                val duration = exoPlayer.duration
                if (duration != C.TIME_UNSET) {
                    val newCurrentTime = exoPlayer.currentPosition.coerceAtLeast(0L)
                    val newTotalDuration = duration.coerceAtLeast(0L)
                    val isAtLiveEdge = state.isLive && (newTotalDuration - newCurrentTime <= LIVE_EDGE_THRESHOLD_MS)

                    if (newCurrentTime != state.currentTime || newTotalDuration != state.totalDuration ||
                        isAtLiveEdge != state.atLiveEdge
                    ) {
                        eventPublisher(
                            LiveStreamContract.UiEvent.OnPlayerStateUpdate(
                                atLiveEdge = isAtLiveEdge,
                                currentTime = newCurrentTime,
                                totalDuration = newTotalDuration,
                            ),
                        )
                    }
                }
            }
            delay(PLAYER_STATE_UPDATE_INTERVAL_MS.milliseconds)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            LiveStreamContent(
                state = state,
                exoPlayer = exoPlayer,
                eventPublisher = eventPublisher,
                paddingValues = paddingValues,
                onClose = onClose,
            )
        },
    )
}

@Composable
private fun LiveStreamContent(
    state: LiveStreamContract.UiState,
    exoPlayer: ExoPlayer,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    paddingValues: PaddingValues,
    onClose: () -> Unit,
) {
    if (state.loading) {
        PrimalLoadingSpinner()
    }

    if (state.streamInfo != null && state.authorProfile != null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colorScheme.surface)
                .padding(paddingValues),
        ) {
            item("MediaPlayer") {
                LiveStreamPlayer(
                    exoPlayer = exoPlayer,
                    streamUrl = state.streamInfo.streamUrl,
                    state = state,
                    onPlayPauseClick = {
                        if (exoPlayer.isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    },
                    onRewind = {
                        val newPosition = (exoPlayer.currentPosition - SEEK_INCREMENT_MS).coerceAtLeast(0L)
                        exoPlayer.seekTo(newPosition)
                    },
                    onForward = {
                        if (state.isLive) {
                            val newPosition = exoPlayer.currentPosition + SEEK_INCREMENT_MS
                            if (newPosition >= state.totalDuration) {
                                exoPlayer.seekToDefaultPosition()
                            } else {
                                exoPlayer.seekTo(newPosition)
                            }
                        } else {
                            val newPosition = (exoPlayer.currentPosition + SEEK_INCREMENT_MS)
                                .coerceAtMost(state.totalDuration)
                            exoPlayer.seekTo(newPosition)
                        }
                    },
                    onClose = onClose,
                    eventPublisher = eventPublisher,
                )
            }

            item("StreamInfo") {
                StreamInfoSection(
                    title = state.streamInfo.title,
                    authorProfile = state.authorProfile,
                    viewers = state.streamInfo.viewers,
                    startedAt = state.streamInfo.startedAt,
                    profileStats = state.profileStats,
                    isFollowed = state.isFollowed,
                    onFollow = {
                        state.profileId?.let {
                            eventPublisher(LiveStreamContract.UiEvent.FollowAction(profileId = it))
                        }
                    },
                    onUnfollow = {
                        state.profileId?.let {
                            eventPublisher(LiveStreamContract.UiEvent.UnfollowAction(profileId = it))
                        }
                    },
                )
            }

            item("LiveChatHeader") {
            }
        }
    }
}
