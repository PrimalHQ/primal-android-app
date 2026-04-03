package net.primal.android.notes.feed.note.ui

import androidx.media3.common.MediaItem

data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val playWhenReady: Boolean = false,
    val isBuffering: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isActiveForMediaId: Boolean = false,
    val play: () -> Unit = {},
    val pause: () -> Unit = {},
    val seekTo: (Long) -> Unit = {},
    val playMediaItem: (MediaItem) -> Unit = {},
)
