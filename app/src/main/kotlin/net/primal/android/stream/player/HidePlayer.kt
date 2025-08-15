package net.primal.android.stream.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect

/**
 * Temporarily hides the stream mini player while this composable is in the composition.
 *
 * This function:
 * - Immediately calls [StreamState.hide] on the current [LocalStreamState] when the composable
 *   enters the composition.
 * - Automatically restores the mini player by calling [StreamState.show] when the composable
 *   leaves the composition (is disposed).
 *
 * This is useful for screens or UI states where the mini player should not be visible,
 * but should reappear automatically once the user navigates away.
 *
 * @return `true` if the stream mini player is currently hidden according to [StreamState.isHidden],
 *         `false` otherwise.
 *
 * @see LocalStreamState
 * @see StreamState.hide
 * @see StreamState.show
 * @see StreamState.isHidden
 */
@Composable
fun hideStreamMiniPlayer(): Boolean {
    val streamState = LocalStreamState.current

    LaunchedEffect(streamState) {
        streamState.hide()
    }
    DisposableEffect(streamState) {
        onDispose { streamState.show() }
    }

    return streamState.isHidden()
}
