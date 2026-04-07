package net.primal.android.core.video

import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import net.primal.android.audio.player.AudioPlayerCommand
import net.primal.android.audio.player.AudioPlayerState
import net.primal.android.audio.player.LocalAudioPlayerState
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

private const val PROGRESS_POLL_INTERVAL_MS = 200L

@Suppress("CyclomaticComplexMethod")
@Composable
fun AudioPlayerOverlay(content: @Composable () -> Unit) {
    val audioState = LocalAudioPlayerState.current
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    var controller by remember { mutableStateOf<MediaController?>(null) }

    DisposableEffect(Unit) {
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
            audioState.isPlaying = c.isPlaying
            audioState.playWhenReady = c.playWhenReady
            audioState.isBuffering = c.playbackState == Player.STATE_BUFFERING
            audioState.durationMs = c.duration.coerceAtLeast(0L)
            audioState.currentPositionMs = c.currentPosition.coerceAtLeast(0L)
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

    LaunchedEffect(controller, audioState.isPlaying) {
        val c = controller ?: return@LaunchedEffect
        while (audioState.isPlaying) {
            audioState.currentPositionMs = c.currentPosition.coerceAtLeast(0L)
            audioState.durationMs = c.duration.coerceAtLeast(0L)
            delay(PROGRESS_POLL_INTERVAL_MS)
        }
    }

    LaunchedEffect(audioState, audioState.commands, controller) {
        audioState.commands.collect { command ->
            val c = controller ?: return@collect
            when (command) {
                AudioPlayerCommand.Play -> c.play()
                AudioPlayerCommand.Pause -> c.pause()
                is AudioPlayerCommand.SeekTo -> c.seekTo(command.positionMs)
                is AudioPlayerCommand.PlayUrl -> {
                    val mediaItem = MediaItem.Builder()
                        .setUri(command.url)
                        .setMediaId(command.url)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(command.title)
                                .setArtist(command.artist)
                                .build(),
                        )
                        .build()
                    c.clearMediaItems()
                    c.setMediaItem(mediaItem)
                    c.prepare()
                    c.play()
                }
            }
        }
    }

    content()
}

fun MediaController.toggle() =
    if (isPlaying) {
        pause()
    } else {
        play()
    }
