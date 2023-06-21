package net.primal.android.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import net.primal.android.theme.colors.ExtraColorScheme
import net.primal.android.theme.colors.ExtraColorSchemeProvider
import net.primal.android.theme.colors.LocalExtraColors


@Composable
fun PrimalTheme(
    theme: PrimalTheme? = null,
    content: @Composable () -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()
    val primalTheme = theme ?: if (isDarkMode) PrimalTheme.Sunset else PrimalTheme.Sunrise

    AdjustSystemColors(primalTheme = primalTheme)

    ExtraColorSchemeProvider(extraColorScheme = primalTheme.extraColorScheme) {
        MaterialTheme(
            colorScheme = primalTheme.colorScheme,
            shapes = PrimalShapes,
            typography = PrimalTypography,
            content = content
        )
    }
}

@Composable
private fun AdjustSystemColors(primalTheme: PrimalTheme) {
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

object AppTheme {

    val colorScheme: ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    val extraColorScheme: ExtraColorScheme
        @Composable
        get() = LocalExtraColors.current

    val typography: Typography
        @Composable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        get() = MaterialTheme.shapes

}
