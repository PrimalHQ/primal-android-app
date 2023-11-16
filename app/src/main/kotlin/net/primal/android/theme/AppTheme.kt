package net.primal.android.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import net.primal.android.core.compose.AdjustSystemColors
import net.primal.android.theme.colors.ExtraColorScheme
import net.primal.android.theme.colors.ExtraColorSchemeProvider
import net.primal.android.theme.colors.LocalExtraColors
import net.primal.android.theme.domain.PrimalTheme

@ReadOnlyComposable
@Composable
fun defaultPrimalTheme(): PrimalTheme {
    return if (isSystemInDarkTheme()) PrimalTheme.Sunset else PrimalTheme.Sunrise
}

@Composable
fun PrimalTheme(primalTheme: PrimalTheme, content: @Composable () -> Unit) {
    AdjustSystemColors(primalTheme = primalTheme)
    ExtraColorSchemeProvider(extraColorScheme = primalTheme.extraColorScheme) {
        MaterialTheme(
            colorScheme = primalTheme.colorScheme,
            shapes = PrimalShapes,
            typography = PrimalTypography,
            content = content,
        )
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
