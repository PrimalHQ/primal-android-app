package net.primal.android.stream.overlay

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import net.primal.android.core.video.rememberPrimalStreamExoPlayer
import net.primal.android.navigation.navigateToWallet
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.stream.LiveStreamContract
import net.primal.android.stream.LiveStreamViewModel
import net.primal.android.stream.player.LocalStreamState
import net.primal.android.stream.player.StreamMode
import net.primal.android.stream.player.StreamStateProvider
import net.primal.android.stream.ui.LiveStreamMiniPlayer
import net.primal.android.stream.ui.LiveStreamScreen

@Composable
fun LiveStreamOverlay(
    navController: NavHostController,
    noteCallbacks: NoteCallbacks,
    content: @Composable () -> Unit,
) {
    val viewModel = hiltViewModel<LiveStreamViewModel>()

    StreamStateProvider {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            LiveStreamOverlay(
                viewModel = viewModel,
                navController = navController,
                noteCallbacks = noteCallbacks,
            )
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
    val uiState = viewModel.state.collectAsState()
    val streamState = LocalStreamState.current

    BackHandler(enabled = streamState.mode is StreamMode.Expanded) {
        streamState.minimize()
    }

    val exoPlayer = rememberPrimalStreamExoPlayer(
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

    when (val mode = streamState.mode) {
        is StreamMode.Expanded -> {
            viewModel.setEvent(LiveStreamContract.UiEvent.StartStream(mode.naddr))

            LiveStreamScreen(
                viewModel = viewModel,
                state = uiState.value,
                exoPlayer = exoPlayer,
                onClose = { streamState.minimize() },
                noteCallbacks = noteCallbacks.withActionAfterCallback { streamState.minimize() },
                onGoToWallet = {
                    navController.navigateToWallet()
                    streamState.minimize()
                },
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
                    viewModel.setEvent(LiveStreamContract.UiEvent.StopStream)
                },
            )
        }

        StreamMode.Closed -> Unit
    }
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
private fun NoteCallbacks.withActionAfterCallback(action: () -> Unit): NoteCallbacks {
    return this.copy(
        onNoteClick = this.onNoteClick?.let { original ->
            { noteId ->
                original(noteId)
                action()
            }
        },
        onNoteReplyClick = this.onNoteReplyClick?.let { original ->
            { noteNevent ->
                original(noteNevent)
                action()
            }
        },
        onNoteQuoteClick = this.onNoteQuoteClick?.let { original ->
            { noteNevent ->
                original(noteNevent)
                action()
            }
        },
        onHighlightReplyClick = this.onHighlightReplyClick?.let { original ->
            { highlightNevent, articleNaddr ->
                original(highlightNevent, articleNaddr)
                action()
            }
        },
        onHighlightQuoteClick = this.onHighlightQuoteClick?.let { original ->
            { highlightNevent, articleNaddr ->
                original(highlightNevent, articleNaddr)
                action()
            }
        },
        onArticleClick = this.onArticleClick?.let { original ->
            { naddr ->
                original(naddr)
                action()
            }
        },
        onArticleReplyClick = this.onArticleReplyClick?.let { original ->
            { naddr ->
                original(naddr)
                action()
            }
        },
        onArticleQuoteClick = this.onArticleQuoteClick?.let { original ->
            { naddr ->
                original(naddr)
                action()
            }
        },
        onProfileClick = this.onProfileClick?.let { original ->
            { profileId ->
                original(profileId)
                action()
            }
        },
        onHashtagClick = this.onHashtagClick?.let { original ->
            { hashtag ->
                original(hashtag)
                action()
            }
        },
        onMediaClick = this.onMediaClick?.let { original ->
            { event ->
                original(event)
                action()
            }
        },
        onPayInvoiceClick = this.onPayInvoiceClick?.let { original ->
            { event ->
                original(event)
                action()
            }
        },
        onEventReactionsClick = this.onEventReactionsClick?.let { original ->
            { eventId, initialTab, articleATag ->
                original(eventId, initialTab, articleATag)
                action()
            }
        },
        onGetPrimalPremiumClick = this.onGetPrimalPremiumClick?.let { original ->
            {
                original()
                action()
            }
        },
        onPrimalLegendsLeaderboardClick = this.onPrimalLegendsLeaderboardClick?.let { original ->
            {
                original()
                action()
            }
        },
    )
}
