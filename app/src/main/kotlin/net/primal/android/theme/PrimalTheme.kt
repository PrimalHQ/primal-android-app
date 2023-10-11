package net.primal.android.theme

import androidx.compose.material3.ColorScheme
import net.primal.android.R
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
    val isDarkTheme: Boolean,
    val colorScheme: ColorScheme,
    val extraColorScheme: ExtraColorScheme,
    val logoId: Int
) {
    Sunset(
        themeName = "sunset",
        inverseThemeName = "sunrise",
        isDarkTheme = true,
        colorScheme = sunsetColorScheme,
        extraColorScheme = sunsetExtraColorScheme,
        logoId = R.drawable.primal_wave_logo_red
    ),

    Midnight(
        themeName = "midnight",
        inverseThemeName = "ice",
        isDarkTheme = true,
        colorScheme = midnightColorScheme,
        extraColorScheme = midnightExtraColorScheme,
        logoId = R.drawable.primal_wave_logo_blue
    ),

    Sunrise(
        themeName = "sunrise",
        inverseThemeName = "sunset",
        isDarkTheme = false,
        colorScheme = sunriseColorScheme,
        extraColorScheme = sunriseExtraColorScheme,
        logoId = R.drawable.primal_wave_logo_red
    ),

    Ice(
        themeName = "ice",
        inverseThemeName = "midnight",
        isDarkTheme = false,
        colorScheme = iceColorScheme,
        extraColorScheme = iceExtraColorScheme,
        logoId = R.drawable.primal_wave_logo_blue
    );

    companion object {
        fun valueOf(themeName: String): PrimalTheme? =
            enumValues<PrimalTheme>().find { it.themeName == themeName }
    }
}
