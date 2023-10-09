package net.primal.android.core.compose.foundation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun keyboardVisibilityAsState(): State<Boolean> {
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val minKeyboardVisibility = with(density) { 128.dp.toPx() }
    val isImeVisible = imeBottom > minKeyboardVisibility
    return rememberUpdatedState(isImeVisible)
}
