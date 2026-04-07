package net.primal.android.audio.player

import android.content.ComponentName
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import net.primal.android.core.service.PrimalMediaSessionService

private const val PROGRESS_POLL_INTERVAL_MS = 200L
const val EXTRA_NOTE_ID = "noteId"

@Composable
fun AudioPlayerStateProvider(content: @Composable () -> Unit) {
    val audioPlayerState = rememberSaveable(saver = AudioPlayerStateSaver) { AudioPlayerState() }

    CompositionLocalProvider(LocalAudioPlayerState provides audioPlayerState) {
        AudioPlayerController(audioPlayerState)
        content()
    }
}

@Suppress("CyclomaticComplexMethod")
@Composable
private fun AudioPlayerController(audioState: AudioPlayerState) {
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
            audioState.updatePlaybackState(
                isPlaying = c.isPlaying,
                playWhenReady = c.playWhenReady,
                isBuffering = c.playbackState == Player.STATE_BUFFERING,
                currentPositionMs = c.currentPosition.coerceAtLeast(0L),
                durationMs = c.duration.coerceAtLeast(0L),
            )
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
            audioState.updatePosition(
                currentPositionMs = c.currentPosition.coerceAtLeast(0L),
                durationMs = c.duration.coerceAtLeast(0L),
            )
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
                    val extras = Bundle().apply {
                        command.noteId?.let { putString(EXTRA_NOTE_ID, it) }
                    }
                    val mediaItem = MediaItem.Builder()
                        .setUri(command.url)
                        .setMediaId(command.url)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(command.title)
                                .setArtist(command.artist)
                                .setExtras(extras)
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
}

val LocalAudioPlayerState = staticCompositionLocalOf<AudioPlayerState> {
    error("No AudioPlayerState provided — did you forget to wrap your UI in AudioPlayerStateProvider?")
}
