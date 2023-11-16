package net.primal.android.core.compose

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun AdjustTemporarilySystemBarColors(statusBarColor: Color? = null, navigationBarColor: Color? = null) {
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

@Composable
fun AdjustSystemColors(primalTheme: PrimalTheme) {
    val view = LocalView.current
    val darkTheme = primalTheme.colorScheme.surface.luminance() < 0.5f
    val systemBarColor = primalTheme.colorScheme.background
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            window.statusBarColor = systemBarColor.toArgb()
            insetsController.isAppearanceLightStatusBars = !darkTheme

            window.navigationBarColor = systemBarColor.toArgb()
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }
}
