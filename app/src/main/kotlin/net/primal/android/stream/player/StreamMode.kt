package net.primal.android.stream.player

sealed interface StreamMode {
    object Hidden : StreamMode
    data class Minimized(val naddr: String) : StreamMode
    data class Expanded(val naddr: String) : StreamMode
}
