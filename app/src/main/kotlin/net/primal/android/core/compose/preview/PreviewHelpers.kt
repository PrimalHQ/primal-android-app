package net.primal.android.core.compose.preview

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.LocalPrimalTheme
import net.primal.android.theme.PrimalRippleTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.ContentDisplaySettings

@Composable
fun PrimalPreview(
    primalTheme: PrimalTheme,
    displaySettings: ContentDisplaySettings = ContentDisplaySettings(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPrimalTheme provides primalTheme,
        LocalRippleTheme provides PrimalRippleTheme,
        LocalContentDisplaySettings provides displaySettings,
    ) {
        PrimalTheme(primalTheme = primalTheme, content = content)
    }
}
