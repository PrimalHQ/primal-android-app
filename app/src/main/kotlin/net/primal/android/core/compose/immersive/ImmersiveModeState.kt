package net.primal.android.core.compose.immersive

import android.view.Window
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Stable
class ImmersiveModeState internal constructor(
    private val window: Window,
    initialValue: Boolean,
) {
    var isImmersive by mutableStateOf(initialValue)
        private set

    private val controller: WindowInsetsControllerCompat
        get() = WindowCompat.getInsetsController(window, window.decorView)

    init {
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    fun show() {
        controller.hide(WindowInsetsCompat.Type.systemBars())
        isImmersive = true
    }

    fun hide() {
        controller.show(WindowInsetsCompat.Type.systemBars())
        isImmersive = false
    }

    fun toggle() {
        if (isImmersive) hide() else show()
    }
}
