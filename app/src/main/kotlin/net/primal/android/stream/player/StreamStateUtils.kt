package net.primal.android.stream.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.dp


@Composable
fun rememberStreamState(): StreamState {
    val streamState = LocalStreamState.current

//    DisposableEffect(streamState) {
//        onDispose {
//            streamState.backgroundOpacity = 1f
//            streamState.bottomPadding = 0.dp
//        }
//    }

    return streamState
}
