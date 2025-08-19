package net.primal.android.stream.player

sealed interface StreamMode {
    object Closed : StreamMode
    data class Hidden(val modeToRestore: StreamMode) : StreamMode
    data class Minimized(val naddr: String) : StreamMode
    data class Expanded(val naddr: String) : StreamMode

    fun resolveNaddr(): String? {
        return when (val mode = this) {
            is Expanded -> mode.naddr
            is Minimized -> mode.naddr
            is Hidden -> mode.modeToRestore.resolveNaddr()
            else -> null
        }
    }
}
