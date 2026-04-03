package net.primal.android.core.video

import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import net.primal.android.core.service.PrimalMediaSessionService
import net.primal.android.notes.feed.note.ui.AudioPlayerState
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

private const val PROGRESS_POLL_INTERVAL_MS = 200L

@Composable
fun rememberAudioPlayerState(mediaId: String): AudioPlayerState {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    var controller by remember(mediaId) { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember(mediaId) { mutableStateOf(false) }
    var playWhenReady by remember(mediaId) { mutableStateOf(false) }
    var isBuffering by remember(mediaId) { mutableStateOf(false) }
    var currentPositionMs by remember(mediaId) { mutableLongStateOf(0L) }
    var durationMs by remember(mediaId) { mutableLongStateOf(0L) }

    DisposableEffect(mediaId) {
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
        val c = controller ?: return@DisposableEffect onDispose { }

        fun updateState() {
            val active = c.currentMediaItem?.mediaId == mediaId
            isPlaying = active && c.isPlaying
            playWhenReady = active && c.playWhenReady
            isBuffering = active && c.playbackState == Player.STATE_BUFFERING
            durationMs = if (active) c.duration.coerceAtLeast(0L) else 0L
            currentPositionMs = if (active) c.currentPosition.coerceAtLeast(0L) else 0L
        }

        updateState()

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) = updateState()
            override fun onPlaybackStateChanged(playbackState: Int) = updateState()
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = updateState()
        }
        c.addListener(listener)
        onDispose { c.removeListener(listener) }
    }

    LaunchedEffect(controller, isPlaying) {
        val c = controller ?: return@LaunchedEffect
        while (isPlaying && c.currentMediaItem?.mediaId == mediaId) {
            currentPositionMs = c.currentPosition.coerceAtLeast(0L)
            durationMs = c.duration.coerceAtLeast(0L)
            delay(PROGRESS_POLL_INTERVAL_MS)
        }
    }

    val progress = if (durationMs > 0L) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val isActive = controller?.currentMediaItem?.mediaId == mediaId

    return AudioPlayerState(
        isPlaying = isPlaying,
        playWhenReady = playWhenReady,
        isBuffering = isBuffering,
        progress = progress,
        currentPositionMs = currentPositionMs,
        durationMs = durationMs,
        isActiveForMediaId = isActive,
        play = { controller?.play() },
        pause = { controller?.pause() },
        seekTo = { positionMs -> controller?.seekTo(positionMs) },
        playMediaItem = { mediaItem ->
            controller?.let { c ->
                c.clearMediaItems()
                c.setMediaItem(mediaItem)
                c.prepare()
                c.play()
            }
        },
    )
}

fun MediaController.toggle() =
    if (isPlaying) {
        pause()
    } else {
        play()
    }
