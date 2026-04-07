package net.primal.android.audio.player

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Stable
@OptIn(ExperimentalAtomicApi::class)
class AudioPlayerState internal constructor(
    initialUrl: String? = null,
    initialTitle: String? = null,
    initialArtist: String? = null,
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    var currentUrl by mutableStateOf(initialUrl)
        private set
    var currentTitle by mutableStateOf(initialTitle)
        private set
    var currentArtist by mutableStateOf(initialArtist)
        private set

    var isPlaying by mutableStateOf(false)
        internal set
    var playWhenReady by mutableStateOf(false)
        internal set
    var isBuffering by mutableStateOf(false)
        internal set
    var currentPositionMs by mutableLongStateOf(0L)
        internal set
    var durationMs by mutableLongStateOf(0L)
        internal set

    val progress: Float
        get() = if (durationMs > 0L) {
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    fun isActiveForUrl(url: String): Boolean = currentUrl == url

    private val _pauseHolders = AtomicInt(0)

    private val _commands = Channel<AudioPlayerCommand>()
    val commands = _commands.receiveAsFlow()
    private fun sendCommand(command: AudioPlayerCommand) = scope.launch { _commands.send(command) }

    fun play(url: String, title: String?, artist: String?) {
        currentUrl = url
        currentTitle = title
        currentArtist = artist
        sendCommand(AudioPlayerCommand.PlayUrl(url = url, title = title, artist = artist))
    }

    fun resume() {
        if (currentUrl != null) {
            sendCommand(AudioPlayerCommand.Play)
        }
    }

    fun pause() {
        sendCommand(AudioPlayerCommand.Pause)
    }

    fun seekTo(positionMs: Long) {
        sendCommand(AudioPlayerCommand.SeekTo(positionMs))
    }

    fun stop() {
        currentUrl = null
        currentTitle = null
        currentArtist = null
        isPlaying = false
        playWhenReady = false
        isBuffering = false
        currentPositionMs = 0L
        durationMs = 0L
        sendCommand(AudioPlayerCommand.Pause)
    }

    fun acquirePause() {
        if (_pauseHolders.fetchAndIncrement() == 0) {
            sendCommand(AudioPlayerCommand.Pause)
        }
    }

    fun releasePause() {
        if (_pauseHolders.decrementAndFetch() <= 0) {
            _pauseHolders.store(0)
            if (currentUrl != null) {
                sendCommand(AudioPlayerCommand.Play)
            }
        }
    }
}
