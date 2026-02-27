package net.primal.android.theme.domain

import androidx.compose.material3.ColorScheme
import net.primal.android.R
import net.primal.android.theme.colors.ExtraColorScheme
import net.primal.android.theme.colors.iceColorScheme
import net.primal.android.theme.colors.iceExtraColorScheme
import net.primal.android.theme.colors.midnightColorScheme
import net.primal.android.theme.colors.midnightExtraColorScheme

enum class PrimalTheme(
    val themeName: String,
    val colorScheme: ColorScheme,
    val extraColorScheme: ExtraColorScheme,
    val isDarkTheme: Boolean,
    val logoId: Int,
) {
    Midnight(
        themeName = "midnight",
        colorScheme = midnightColorScheme,
        extraColorScheme = midnightExtraColorScheme,
        isDarkTheme = true,
        logoId = R.drawable.primal_wave_logo_winter,
    ),

    Ice(
        themeName = "ice",
        colorScheme = iceColorScheme,
        extraColorScheme = iceExtraColorScheme,
        isDarkTheme = false,
        logoId = R.drawable.primal_wave_logo_winter,
    ),
    ;

    val inverse: PrimalTheme
        get() = when (this) {
            Midnight -> Ice
            Ice -> Midnight
        }

    companion object {
        fun valueOf(themeName: String): PrimalTheme? {
            return when (themeName) {
                // Migration: map removed themes to their closest remaining equivalents
                // for users who still have these values persisted in DataStore.
                "sunset" -> Midnight
                "sunrise" -> Ice
                else -> enumValues<PrimalTheme>().find { it.themeName == themeName }
            }
        }
    }
}
