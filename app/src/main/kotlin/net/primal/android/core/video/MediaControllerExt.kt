package net.primal.android.core.video

import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import net.primal.android.core.service.PrimalMediaSessionService
import net.primal.domain.nostr.Naddr

@Composable
fun rememberManagedMediaController(
    streamNaddr: Naddr,
    onIsPlayingChanged: (Long?, Boolean) -> Unit,
    onPlaybackStateChanged: (Long?, Int) -> Unit,
    onPlayerError: (errorCode: Int) -> Unit,
): MediaController? {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }

    val latestOnIsPlayingChanged by rememberUpdatedState(onIsPlayingChanged)
    val latestOnPlaybackStateChanged by rememberUpdatedState(onPlaybackStateChanged)
    val latestOnPlayerError by rememberUpdatedState(onPlayerError)

    var controller by remember(streamNaddr) { mutableStateOf<MediaController?>(null) }

    DisposableEffect(streamNaddr) {
        val token = SessionToken(
            appContext,
            ComponentName(appContext, PrimalMediaSessionService::class.java),
        )
        val future = MediaController.Builder(appContext, token).buildAsync()

        val setController = { runCatching { controller = future.get() }.getOrDefault(Unit) }
        future.addListener(setController, MoreExecutors.directExecutor())

        onDispose {
            controller?.release()
            MediaController.releaseFuture(future)
            controller = null
        }
    }

    DisposableEffect(controller) {
        if (controller == null) return@DisposableEffect onDispose { }

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                latestOnIsPlayingChanged(controller?.currentPosition, isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                latestOnPlaybackStateChanged(controller?.duration, playbackState)
            }

            override fun onPlayerError(error: PlaybackException) {
                latestOnPlayerError(error.errorCode)
            }
        }
        controller?.addListener(listener)
        onDispose {
            controller?.removeListener(listener)
            controller?.clearMediaItems()
        }
    }

    return controller
}

fun MediaController.toggle() =
    if (isPlaying) {
        pause()
    } else {
        play()
    }
