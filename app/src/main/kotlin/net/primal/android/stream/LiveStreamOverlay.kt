package net.primal.android.stream

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.core.video.rememberPrimalStreamExoPlayer
import net.primal.android.navigation.navigateToChat
import net.primal.android.navigation.navigateToProfileEditor
import net.primal.android.navigation.navigateToProfileQrCodeViewer
import net.primal.android.navigation.navigateToWallet
import net.primal.android.navigation.navigateToWalletCreateTransaction
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.stream.di.rememberLiveStreamViewModel
import net.primal.android.stream.player.LocalStreamState
import net.primal.android.stream.player.StreamMode
import net.primal.android.stream.player.StreamState
import net.primal.android.stream.player.StreamStateProvider
import net.primal.android.stream.ui.LiveStreamMiniPlayer
import net.primal.android.stream.ui.LiveStreamScreen

@Composable
fun LiveStreamOverlay(
    navController: NavHostController,
    noteCallbacks: NoteCallbacks,
    content: @Composable () -> Unit,
) {
    StreamStateProvider {
        val streamState = LocalStreamState.current
        val naddrUri: String? = streamState.mode.resolveNaddr()
        val liveStreamViewModel = rememberLiveStreamViewModel(naddrUri)

        Box(modifier = Modifier.fillMaxSize()) {
            content()

            if (liveStreamViewModel != null) {
                LiveStreamOverlay(
                    viewModel = liveStreamViewModel,
                    navController = navController,
                    noteCallbacks = noteCallbacks,
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun LiveStreamOverlay(
    viewModel: LiveStreamViewModel,
    navController: NavHostController,
    noteCallbacks: NoteCallbacks,
) {
    val streamState = LocalStreamState.current
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, noteCallbacks, streamState) {
        viewModel.effect.collectLatest {
            when (it) {
                LiveStreamContract.SideEffect.StreamDeleted -> {
                    streamState.stop()
                }
            }
        }
    }

    BackHandler(enabled = streamState.mode is StreamMode.Expanded) {
        streamState.minimize()
    }

    val exoPlayer = rememberPrimalStreamExoPlayer(
        streamNaddr = viewModel.streamNaddr,
        onIsPlayingChanged = { isPlaying ->
            viewModel.setEvent(LiveStreamContract.UiEvent.OnPlayerStateUpdate(isPlaying = isPlaying))
        },
        onPlaybackStateChanged = { playbackState ->
            viewModel.setEvent(
                LiveStreamContract.UiEvent.OnPlayerStateUpdate(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                ),
            )
        },
    )

    when (streamState.mode) {
        is StreamMode.Expanded -> {
            val callbacks = rememberLiveStreamScreenCallbacks(
                navController = navController,
                noteCallbacks = noteCallbacks,
                streamState = streamState,
            )

            ApplyEdgeToEdge()
            LiveStreamScreen(
                eventPublisher = viewModel::setEvent,
                state = uiState.value,
                exoPlayer = exoPlayer,
                callbacks = callbacks,
            )
        }

        is StreamMode.Minimized, is StreamMode.Hidden -> {
            LiveStreamMiniPlayer(
                state = uiState.value,
                exoPlayer = exoPlayer,
                onExpandStream = { streamState.expand() },
                onStopStream = {
                    exoPlayer.stop()
                    streamState.stop()
                },
            )
        }

        StreamMode.Closed -> Unit
    }
}

@Composable
private fun rememberLiveStreamScreenCallbacks(
    navController: NavHostController,
    noteCallbacks: NoteCallbacks,
    streamState: StreamState,
): LiveStreamContract.ScreenCallbacks {
    return remember(navController, noteCallbacks, streamState) {
        LiveStreamContract.ScreenCallbacks(
            onClose = { streamState.minimize() },
            onGoToWallet = {
                navController.navigateToWallet()
                streamState.minimize()
            },
            onEditProfileClick = {
                navController.navigateToProfileEditor()
                streamState.minimize()
            },
            onMessageClick = { profileId ->
                navController.navigateToChat(profileId)
                streamState.minimize()
            },
            onDrawerQrCodeClick = { profileId ->
                navController.navigateToProfileQrCodeViewer(profileId)
                streamState.minimize()
            },
            onQuoteStreamClick = { naddr ->
                noteCallbacks.onStreamQuoteClick?.invoke(naddr)
                streamState.minimize()
            },
            onProfileClick = { profileId ->
                noteCallbacks.onProfileClick?.invoke(profileId)
                streamState.minimize()
            },
            onHashtagClick = { hashtag ->
                noteCallbacks.onHashtagClick?.invoke(hashtag)
                streamState.minimize()
            },
            onEventReactionsClick = { eventId, initialTab, articleATag ->
                noteCallbacks.onEventReactionsClick?.invoke(eventId, initialTab, articleATag)
                streamState.minimize()
            },
            onSendWalletTx = { draftTx ->
                navController.navigateToWalletCreateTransaction(draftTx)
                streamState.minimize()
            },
        )
    }
}
