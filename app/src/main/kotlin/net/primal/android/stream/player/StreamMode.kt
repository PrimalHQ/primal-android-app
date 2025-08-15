package net.primal.android.stream.player

sealed interface StreamMode {
    object Closed : StreamMode
    data class Hidden(val modeToRestore: StreamMode) : StreamMode
    data class Minimized(val naddr: String) : StreamMode
    data class Expanded(val naddr: String) : StreamMode
}
