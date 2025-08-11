package net.primal.android.stream.player

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

@Stable
class StreamState internal constructor() {
    private var _mode by mutableStateOf<StreamMode>(StreamMode.Hidden)

    var bottomPadding by mutableStateOf(0.dp)

    var backgroundOpacity by mutableFloatStateOf(1f)

    val mode: StreamMode get() = _mode

    fun isActive() = mode != StreamMode.Hidden

    fun play(naddr: String) {
        _mode = StreamMode.Expanded(naddr)
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

    fun stop() {
        _mode = StreamMode.Hidden
    }

    fun reset() {
        bottomPadding = 0.dp
        backgroundOpacity = 1f
    }
}
