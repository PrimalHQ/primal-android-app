package net.primal.android.core.compose

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

@Composable
fun AdjustSystemBarColors(
    statusBarColor: Color? = null,
    navigationBarColor: Color? = null,
) {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val originalStatusBarColor = window.statusBarColor
        val originalNavigationBarColor = window.navigationBarColor
        if (statusBarColor != null) window.statusBarColor = statusBarColor.toArgb()
        if (navigationBarColor != null) window.navigationBarColor = navigationBarColor.toArgb()
        onDispose {
            if (statusBarColor != null) window.statusBarColor = originalStatusBarColor
            if (navigationBarColor != null) window.navigationBarColor = originalNavigationBarColor
        }
    }
}
