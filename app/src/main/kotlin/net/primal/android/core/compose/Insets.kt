package net.primal.android.core.compose

import android.app.Activity
import android.view.View
import androidx.compose.animation.animateColorAsState
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
fun ApplySystemBarColors(statusBarColor: Color? = null, navigationBarColor: Color? = null) {
    val view = LocalView.current

    val targetStatusBarColor = statusBarColor?.let { animateColorAsState(it, label = "statusBarColorAnim") }
    val isDarkStatusBar = targetStatusBarColor?.value?.let { it.luminance() < 0.5f }

    val targetNavigationBarColor = navigationBarColor?.let {
        animateColorAsState(targetValue = it, label = "navigationBarColorAnim")
    }
    val isDarkNavigationBar = targetNavigationBarColor?.value?.let { it.luminance() < 0.5f }

    SideEffect {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)

        targetStatusBarColor?.value?.let { window.statusBarColor = it.toArgb() }
        isDarkStatusBar?.let { insetsController.isAppearanceLightStatusBars = !it }

        targetNavigationBarColor?.value?.let { window.navigationBarColor = it.toArgb() }
        isDarkNavigationBar?.let { insetsController.isAppearanceLightNavigationBars = !it }
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

fun applySystemColors(primalTheme: PrimalTheme, localView: View) {
    val darkTheme = primalTheme.colorScheme.surface.luminance() < 0.5f
    val systemBarColor = primalTheme.colorScheme.background
    val window = (localView.context as Activity).window
    val insetsController = WindowCompat.getInsetsController(window, localView)

    window.statusBarColor = systemBarColor.toArgb()
    insetsController.isAppearanceLightStatusBars = !darkTheme

    window.navigationBarColor = systemBarColor.toArgb()
    insetsController.isAppearanceLightNavigationBars = !darkTheme
}

fun applySystemColors(
    statusBarColor: Color? = null,
    navigationBarColor: Color? = null,
    localView: View,
) {
    val isDarkStatusBar = statusBarColor?.let { it.luminance() < 0.5f }
    val isDarkNavigationBar = navigationBarColor?.let { it.luminance() < 0.5f }

    val window = (localView.context as Activity).window
    val insetsController = WindowCompat.getInsetsController(window, localView)

    statusBarColor?.let { window.statusBarColor = it.toArgb() }
    isDarkStatusBar?.let { insetsController.isAppearanceLightStatusBars = !it }

    navigationBarColor?.let { window.navigationBarColor = it.toArgb() }
    isDarkNavigationBar?.let { insetsController.isAppearanceLightNavigationBars = !it }
}
