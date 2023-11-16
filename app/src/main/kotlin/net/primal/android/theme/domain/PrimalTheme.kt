package net.primal.android.theme.domain

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
    val colorScheme: ColorScheme,
    val extraColorScheme: ExtraColorScheme,
    val inverseThemeName: String,
    val isDarkTheme: Boolean,
    val accent: PrimalAccent,
) {
    Sunset(
        themeName = "sunset",
        colorScheme = sunsetColorScheme,
        extraColorScheme = sunsetExtraColorScheme,
        inverseThemeName = "sunrise",
        isDarkTheme = true,
        accent = PrimalAccent.Summer,
    ),

    Midnight(
        themeName = "midnight",
        colorScheme = midnightColorScheme,
        extraColorScheme = midnightExtraColorScheme,
        inverseThemeName = "ice",
        isDarkTheme = true,
        accent = PrimalAccent.Winter,
    ),

    Sunrise(
        themeName = "sunrise",
        colorScheme = sunriseColorScheme,
        extraColorScheme = sunriseExtraColorScheme,
        inverseThemeName = "sunset",
        isDarkTheme = false,
        accent = PrimalAccent.Summer,
    ),

    Ice(
        themeName = "ice",
        colorScheme = iceColorScheme,
        extraColorScheme = iceExtraColorScheme,
        inverseThemeName = "midnight",
        isDarkTheme = false,
        accent = PrimalAccent.Winter,
    ),
    ;

    companion object {
        fun valueOf(themeName: String): PrimalTheme? = enumValues<PrimalTheme>().find { it.themeName == themeName }
    }
}
