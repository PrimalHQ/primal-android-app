package net.primal.android.audio.player

import androidx.compose.runtime.saveable.Saver

val AudioPlayerStateSaver: Saver<AudioPlayerState, Any> = Saver(
    save = { state ->
        arrayListOf(
            state.currentUrl,
            state.currentTitle,
            state.currentArtist,
        )
    },
    restore = { raw ->
        @Suppress("UNCHECKED_CAST")
        val list = raw as ArrayList<*>
        AudioPlayerState(
            initialUrl = list[0] as? String,
            initialTitle = list[1] as? String,
            initialArtist = list[2] as? String,
        )
    },
)
