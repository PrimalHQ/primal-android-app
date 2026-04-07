package net.primal.android.audio.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect

/**
 * Pauses the audio player while in composition, and resumes it on dispose.
 *
 * Useful for screens that show their own media (e.g. video gallery) to prevent
 * overlapping playback. Supports multiple instances: playback resumes only after
 * the last pause holder is released.
 */
@Composable
fun PauseAudioPlayer() {
    val audioState = LocalAudioPlayerState.current

    LaunchedEffect(audioState) {
        audioState.acquirePause()
    }

    DisposableEffect(audioState) {
        onDispose {
            audioState.releasePause()
        }
    }
}
