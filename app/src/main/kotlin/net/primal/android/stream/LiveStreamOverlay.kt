@file:kotlin.OptIn(ExperimentalSharedTransitionApi::class)

package net.primal.android.stream

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.core.compose.animatableSaver
import net.primal.android.core.video.rememberPrimalStreamExoPlayer
import net.primal.android.navigation.navigateToChat
import net.primal.android.navigation.navigateToProfileEditor
import net.primal.android.navigation.navigateToProfileQrCodeViewer
import net.primal.android.navigation.navigateToWallet
import net.primal.android.navigation.navigateToWalletCreateTransaction
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.stream.LiveStreamContract.UiEvent
import net.primal.android.stream.di.rememberLiveStreamViewModel
import net.primal.android.stream.player.LocalStreamState
import net.primal.android.stream.player.PlayerCommand
import net.primal.android.stream.player.StreamMode
import net.primal.android.stream.player.StreamState
import net.primal.android.stream.player.StreamStateProvider
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_HEIGHT
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_WIDTH
import net.primal.android.stream.ui.LiveStreamMiniPlayer
import net.primal.android.stream.ui.LiveStreamScreen
import net.primal.android.stream.ui.PADDING

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
                LaunchedEffect(liveStreamViewModel, noteCallbacks, streamState) {
                    liveStreamViewModel.effect.collectLatest {
                        when (it) {
                            LiveStreamContract.SideEffect.StreamDeleted -> streamState.stop()
                        }
                    }
                }

                LiveStreamOverlay(
                    viewModel = liveStreamViewModel,
                    navController = navController,
                    noteCallbacks = noteCallbacks,
                )
            }
        }
    }
}

@Composable
private fun LiveStreamOverlay(
    viewModel: LiveStreamViewModel,
    navController: NavHostController,
    noteCallbacks: NoteCallbacks,
) {
    val streamState = LocalStreamState.current
    val uiState = viewModel.state.collectAsState()

    val exoPlayer = rememberPrimalStreamExoPlayer(
        streamNaddr = viewModel.streamNaddr,
        onIsPlayingChanged = { exoPlayer, isPlaying ->
            viewModel.setEvent(
                UiEvent.OnPlayerStateUpdate(isPlaying = isPlaying, currentTime = exoPlayer.currentPosition),
            )
        },
        onPlaybackStateChanged = { exoPlayer, playbackState ->
            viewModel.setEvent(
                UiEvent.OnPlayerStateUpdate(isBuffering = playbackState == Player.STATE_BUFFERING),
            )
            when (playbackState) {
                Player.STATE_READY -> {
                    exoPlayer.duration.takeIf { it > 0L }?.let { duration ->
                        viewModel.setEvent(UiEvent.OnPlayerStateUpdate(totalDuration = duration))
                    }
                }

                Player.STATE_ENDED -> {
                    viewModel.setEvent(UiEvent.OnVideoEnded)
                }
            }
        },
        onPlayerError = {
            viewModel.setEvent(UiEvent.OnVideoUnavailable)
        },
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                exoPlayer.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = streamState.mode is StreamMode.Expanded) {
        if (uiState.value.playerState.isVideoFinished) {
            exoPlayer.stop()
            streamState.stop()
        } else {
            streamState.minimize()
        }
    }

    LaunchedEffect(streamState, streamState.commands, exoPlayer) {
        streamState.commands.collect { command ->
            when (command) {
                PlayerCommand.Play -> exoPlayer.play()
                PlayerCommand.Pause -> exoPlayer.pause()
            }
        }
    }

    LiveStreamAnimatedContent(
        streamState = streamState,
        navController = navController,
        noteCallbacks = noteCallbacks,
        viewModel = viewModel,
        uiState = uiState,
        exoPlayer = exoPlayer,
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun LiveStreamAnimatedContent(
    streamState: StreamState,
    navController: NavHostController,
    noteCallbacks: NoteCallbacks,
    viewModel: LiveStreamViewModel,
    uiState: State<LiveStreamContract.UiState>,
    exoPlayer: ExoPlayer,
) {
    val localDensity = LocalDensity.current
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val playerWidth = displayMetrics.widthPixels / 2
    val playerHeight = playerWidth / (VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT)
    val paddingPx = with(localDensity) { PADDING.toPx() }

    val offsetX = rememberSaveable(saver = animatableSaver()) { Animatable(paddingPx) }
    val offsetY = rememberSaveable(saver = animatableSaver()) {
        Animatable(displayMetrics.heightPixels - streamState.bottomBarHeight - playerHeight - paddingPx)
    }

    val isAtBottom = rememberSaveable { mutableStateOf(true) }
    val isAtTop = rememberSaveable { mutableStateOf(false) }

    SharedTransitionLayout {
        AnimatedContent(targetState = streamState.mode) { streamMode ->
            when (streamMode) {
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
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }

                is StreamMode.Minimized, is StreamMode.Hidden -> {
                    LiveStreamMiniPlayer(
                        state = uiState.value,
                        exoPlayer = exoPlayer,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        isAtTop = isAtTop,
                        isAtBottom = isAtBottom,
                        onExpandStream = { streamState.expand() },
                        onStopStream = {
                            exoPlayer.stop()
                            streamState.stop()
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }

                StreamMode.Closed -> Unit
            }
        }
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
