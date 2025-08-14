package net.primal.android.stream.overlay

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
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
import net.primal.android.theme.AppTheme
import timber.log.Timber

private val bottomBarRoutes = listOf("home", "reads", "wallet", "notifications", "explore")

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
    val localDensity = LocalDensity.current

    val animatedPadding by animateDpAsState(streamState.bottomPadding)
    var miniPlayerHeight by remember { mutableStateOf(0.dp) }

    streamState.miniPlayerHeight = miniPlayerHeight
    Timber.d(streamState.bottomPadding.toString())

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
                noteCallbacks = noteCallbacks,
                onGoToWallet = { navController.navigateToWallet() },
            )
        }

        is StreamMode.Minimized -> {
            LiveStreamMiniPlayer(
                modifier = Modifier.padding(bottom = animatedPadding),
                state = uiState.value,
                exoPlayer = exoPlayer,
                onExpandStream = { streamState.expand() },
                onStopStream = {
                    exoPlayer.stop()
                    streamState.stop()
                },
            )
        }

        StreamMode.Hidden -> Unit
    }
}

private fun NavBackStackEntry?.isTopLevelRoute() =
    bottomBarRoutes.any { this?.destination?.route?.startsWith(it) == true }
