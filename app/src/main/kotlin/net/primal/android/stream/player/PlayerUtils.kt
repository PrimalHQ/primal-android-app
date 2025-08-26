package net.primal.android.stream.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect

/**
 * Temporarily hides the stream mini player while this composable is in the composition.
 *
 * This function:
 * - Immediately calls [StreamState.acquireHide] on the current [LocalStreamState] when the composable
 *   enters the composition.
 * - Automatically restores the mini player by calling [StreamState.releaseHide] when the composable
 *   leaves the composition (is disposed).
 *
 * This is useful for screens or UI states where the mini player should not be visible,
 * but should reappear automatically once the user navigates away.
 *
 * @return `true` if the stream mini player is currently hidden according to [StreamState.isHidden],
 *         `false` otherwise.
 *
 * @see LocalStreamState
 * @see StreamState.acquireHide
 * @see StreamState.releaseHide
 * @see StreamState.isHidden
 */
@Composable
fun hideStreamMiniPlayer(): Boolean {
    val streamState = LocalStreamState.current

    LaunchedEffect(streamState) {
        streamState.acquireHide()
    }
    DisposableEffect(streamState) {
        onDispose { streamState.releaseHide() }
    }

    return streamState.isHidden()
}

/**
 * Pauses the stream mini player while in composition, and resumes it on dispose.
 *
 * Useful for screens that show their own media (e.g. video gallery) to prevent
 * overlapping playback. Supports multiple instances: playback resumes only after
 * the last pause holder is released.
 */
@Composable
fun PauseStreamMiniPlayer() {
    val streamState = LocalStreamState.current

    LaunchedEffect(streamState) {
        streamState.acquirePause()
    }

    DisposableEffect(streamState) {
        onDispose {
            streamState.releasePause()
        }
    }
}
