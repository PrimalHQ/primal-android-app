package net.primal.android.stream.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf

@Composable
fun StreamStateProvider(content: @Composable () -> Unit) {
    val streamState = rememberSaveable(saver = StreamStateSaver) { StreamState() }

    CompositionLocalProvider(LocalStreamState provides streamState) {
        content()
    }
}

val LocalStreamState = staticCompositionLocalOf<StreamState> {
    error("No StreamState provided — did you forget to wrap your UI in StreamStateProvider?")
}
