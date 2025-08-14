package net.primal.android.stream.player

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class StreamState internal constructor() {
    private var _mode by mutableStateOf<StreamMode>(StreamMode.Hidden)

    var bottomBarHeight by mutableIntStateOf(0)
    var topBarHeight by mutableIntStateOf(0)

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
}
