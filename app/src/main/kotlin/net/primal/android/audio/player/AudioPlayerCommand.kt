package net.primal.android.audio.player

sealed interface AudioPlayerCommand {
    data object Play : AudioPlayerCommand
    data object Pause : AudioPlayerCommand
    data class SeekTo(val positionMs: Long) : AudioPlayerCommand
    data class PlayUrl(val url: String, val title: String?, val artist: String?) : AudioPlayerCommand
}
