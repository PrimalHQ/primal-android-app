package net.primal.android.stream

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.core.video.rememberPrimalStreamExoPlayer
import net.primal.android.navigation.navigateToWallet
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.stream.di.rememberLiveStreamViewModel
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
    StreamStateProvider {
        val streamState = LocalStreamState.current
        val naddrUri: String? = when (val mode = streamState.mode) {
            is StreamMode.Expanded -> mode.naddr
            is StreamMode.Minimized -> mode.naddr
            else -> null
        }

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
                is LiveStreamContract.SideEffect.NavigateToQuote -> {
                    noteCallbacks.onNoteQuoteClick?.invoke(it.naddr)
                }

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
            ApplyEdgeToEdge()
            LiveStreamScreen(
                eventPublisher = viewModel::setEvent,
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
