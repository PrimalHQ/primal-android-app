package net.primal.android.theme

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.primal.android.LocalPrimalTheme

object PrimalRippleTheme : RippleTheme {

    @Composable
    override fun defaultColor(): Color = AppTheme.colorScheme.outline

    @Composable
    override fun rippleAlpha(): RippleAlpha =
        RippleTheme.defaultRippleAlpha(
            contentColor = AppTheme.colorScheme.outline,
            lightTheme = !LocalPrimalTheme.current.isDarkTheme,
        )
}
