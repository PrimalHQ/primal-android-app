package net.primal.android.audio.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect

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
