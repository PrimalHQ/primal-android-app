package net.primal.android.core.compose.immersive

import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun rememberImmersiveModeState(window: Window, initialValue: Boolean = false): ImmersiveModeState {
    val currentValueState = rememberSaveable { mutableStateOf(initialValue) }
    val state = remember(window) { ImmersiveModeState(window, currentValueState.value) }

    LaunchedEffect(state.isImmersive) {
        currentValueState.value = state.isImmersive
    }

    DisposableEffect(state) {
        onDispose { state.hide() }
    }

    return state
}
