package net.primal.android.audio.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf

@Composable
fun AudioPlayerStateProvider(content: @Composable () -> Unit) {
    val audioPlayerState = rememberSaveable(saver = AudioPlayerStateSaver) { AudioPlayerState() }

    CompositionLocalProvider(LocalAudioPlayerState provides audioPlayerState) {
        content()
    }
}

val LocalAudioPlayerState = staticCompositionLocalOf<AudioPlayerState> {
    error("No AudioPlayerState provided — did you forget to wrap your UI in AudioPlayerStateProvider?")
}
