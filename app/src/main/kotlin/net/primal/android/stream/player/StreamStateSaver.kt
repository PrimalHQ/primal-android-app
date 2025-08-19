package net.primal.android.stream.player

import androidx.compose.runtime.saveable.Saver

private const val MODE_CLOSED = 0
private const val MODE_HIDDEN = 1
private const val MODE_MINIMIZED = 2
private const val MODE_EXPANDED = 3

private fun saveMode(mode: StreamMode): Any {
    return when (mode) {
        StreamMode.Closed -> arrayListOf(MODE_CLOSED, null)
        is StreamMode.Hidden -> arrayListOf(
            MODE_HIDDEN,
            saveMode(mode.modeToRestore),
        )

        is StreamMode.Minimized -> arrayListOf(MODE_MINIMIZED, mode.naddr)
        is StreamMode.Expanded -> arrayListOf(MODE_EXPANDED, mode.naddr)
    }
}

@Suppress("UNCHECKED_CAST")
private fun restoreMode(raw: Any): StreamMode {
    val list = raw as ArrayList<*>
    val tag = list[0] as Int
    val payload = list[1]
    return when (tag) {
        MODE_CLOSED -> StreamMode.Closed
        MODE_HIDDEN -> StreamMode.Hidden(modeToRestore = restoreMode(payload))
        MODE_MINIMIZED -> StreamMode.Minimized(naddr = payload as String)
        MODE_EXPANDED -> StreamMode.Expanded(naddr = payload as String)
        else -> StreamMode.Closed
    }
}

val StreamStateSaver: Saver<StreamState, Any> = Saver(
    save = { state ->
        arrayListOf(
            saveMode(state.mode),
            state.bottomBarHeight,
            state.topBarHeight,
        )
    },
    restore = { raw ->
        val list = raw as ArrayList<*>
        val mode = restoreMode(list[0])
        val bottom = list[1] as Int
        val top = list[2] as Int
        StreamState(
            initialMode = mode,
            initialBottomBarHeight = bottom,
            initialTopBarHeight = top,
        )
    },
)
