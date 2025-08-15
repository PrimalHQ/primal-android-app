package net.primal.android.core.compose

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * A read-only snapshot of the current keyboard (IME) state.
 */
data class KeyboardState(
    val isVisible: Boolean,
    val heightPx: Int,
    val heightDp: Dp,
)

/**
 * Observe the keyboard (IME) visibility and height as a State.
 *
 * - Works with Jetpack Compose insets.
 * - `heightPx` is the bottom inset of WindowInsets.ime in **pixels**.
 * - `heightDp` is the same value in **dp**.
 */
@ExperimentalLayoutApi
@Composable
fun rememberKeyboardState(): State<KeyboardState> {
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val imeVisible = WindowInsets.isImeVisible

    return remember(density, imeVisible, imeInsets) {
        derivedStateOf {
            val heightPx = imeInsets.getBottom(density)
            val heightDp = with(density) { heightPx.toDp() }
            KeyboardState(
                isVisible = imeVisible,
                heightPx = heightPx,
                heightDp = heightDp,
            )
        }
    }
}
