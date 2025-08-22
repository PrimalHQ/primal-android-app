package net.primal.android.stream.player

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
class StreamState internal constructor(
    initialMode: StreamMode = StreamMode.Closed,
    initialBottomBarHeight: Int = 0,
    initialTopBarHeight: Int = 0,
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var _mode by mutableStateOf(initialMode)

    var bottomBarHeight by mutableIntStateOf(initialBottomBarHeight)
    var topBarHeight by mutableIntStateOf(initialTopBarHeight)

    val mode: StreamMode get() = _mode

    private val _pauseHolders = AtomicInt(0)
    private val _hideHolders = AtomicInt(0)

    private val _commands = Channel<PlayerCommand>()
    val commands = _commands.receiveAsFlow()
    private fun setCommand(command: PlayerCommand) = scope.launch { _commands.send(command) }

    fun isActive() = mode != StreamMode.Closed

    fun isHidden() = mode is StreamMode.Hidden

    fun start(naddr: String) {
        _mode = StreamMode.Expanded(naddr)
    }

    fun releasePause() {
        if (_pauseHolders.decrementAndFetch() <= 0) {
            _pauseHolders.store(0)
            setCommand(command = PlayerCommand.Play)
        }
    }

    fun acquirePause() {
        if (_pauseHolders.fetchAndIncrement() == 0) {
            setCommand(command = PlayerCommand.Pause)
        }
    }

    fun minimize() {
        val current = _mode
        if (current is StreamMode.Expanded) {
            _mode = StreamMode.Minimized(current.naddr)
        }
    }

    fun expand() {
        val current = _mode
        if (current is StreamMode.Minimized) {
            _mode = StreamMode.Expanded(current.naddr)
        }
    }

    fun acquireHide() {
        if (_hideHolders.fetchAndIncrement() == 0) {
            val current = _mode
            if (current !is StreamMode.Hidden) {
                _mode = StreamMode.Hidden(modeToRestore = current)
            }
        }
    }

    fun releaseHide() {
        if (_hideHolders.decrementAndFetch() <= 0) {
            _hideHolders.store(0)
            val current = _mode
            if (current is StreamMode.Hidden) {
                _mode = current.modeToRestore
            }
        }
    }

    fun stop() {
        _mode = StreamMode.Closed
    }
}
