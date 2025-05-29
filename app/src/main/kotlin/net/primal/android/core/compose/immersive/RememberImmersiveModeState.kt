package net.primal.android.core.compose.immersive

import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun rememberImmersiveModeState(window: Window, initialValue: Boolean = false): ImmersiveModeState {
    val state = remember(window) { ImmersiveModeState(window, initialValue) }

    LaunchedEffect(Unit) {
        if (initialValue) state.show() else state.hide()
    }

    DisposableEffect(state) {
        onDispose { state.hide() }
    }

    return state
}
