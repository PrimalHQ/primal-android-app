package net.primal.android.theme

import androidx.compose.material3.ColorScheme
import net.primal.android.theme.colors.ExtraColorScheme
import net.primal.android.theme.colors.iceColorScheme
import net.primal.android.theme.colors.iceExtraColorScheme
import net.primal.android.theme.colors.midnightColorScheme
import net.primal.android.theme.colors.midnightExtraColorScheme
import net.primal.android.theme.colors.sunriseColorScheme
import net.primal.android.theme.colors.sunriseExtraColorScheme
import net.primal.android.theme.colors.sunsetColorScheme
import net.primal.android.theme.colors.sunsetExtraColorScheme

enum class PrimalTheme(
    val themeName: String,
    val inverseThemeName: String,
    val colorScheme: ColorScheme,
    val extraColorScheme: ExtraColorScheme,
) {
    Sunset(
        themeName = "sunset",
        inverseThemeName = "sunrise",
        colorScheme = sunsetColorScheme,
        extraColorScheme = sunsetExtraColorScheme,
    ),

    Sunrise(
        themeName = "sunrise",
        inverseThemeName = "sunset",
        colorScheme = sunriseColorScheme,
        extraColorScheme = sunriseExtraColorScheme,
    ),

    Midnight(
        themeName = "midnight",
        inverseThemeName = "ice",
        colorScheme = midnightColorScheme,
        extraColorScheme = midnightExtraColorScheme,
    ),

    Ice(
        themeName = "ice",
        inverseThemeName = "midnight",
        colorScheme = iceColorScheme,
        extraColorScheme = iceExtraColorScheme,
    );

    companion object {
        fun valueOf(themeName: String): PrimalTheme? =
            enumValues<PrimalTheme>().find { it.themeName == themeName }
    }
}
