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
    val theme: String,
    val inverseTheme: String,
    val colorScheme: ColorScheme,
    val extraColorScheme: ExtraColorScheme,
) {
    Sunset(
        theme = "sunset",
        inverseTheme = "sunrise",
        colorScheme = sunsetColorScheme,
        extraColorScheme = sunsetExtraColorScheme,
    ),

    Sunrise(
        theme = "sunrise",
        inverseTheme = "sunset",
        colorScheme = sunriseColorScheme,
        extraColorScheme = sunriseExtraColorScheme,
    ),

    Midnight(
        theme = "midnight",
        inverseTheme = "ice",
        colorScheme = midnightColorScheme,
        extraColorScheme = midnightExtraColorScheme,
    ),

    Ice(
        theme = "ice",
        inverseTheme = "midnight",
        colorScheme = iceColorScheme,
        extraColorScheme = iceExtraColorScheme,
    );

    companion object {
        fun valueOf(themeName: String): PrimalTheme? =
            enumValues<PrimalTheme>().find { it.theme == themeName }
    }
}
