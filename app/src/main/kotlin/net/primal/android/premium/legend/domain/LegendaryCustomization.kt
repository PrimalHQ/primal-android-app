package net.primal.android.premium.legend.domain

import net.primal.domain.PrimalLegendProfile

data class LegendaryCustomization(
    val avatarGlow: Boolean = false,
    val customBadge: Boolean = false,
    val legendaryStyle: LegendaryStyle? = null,
    val currentShoutout: String? = null,
    val inLeaderboard: Boolean? = null,
)

fun PrimalLegendProfile.asLegendaryCustomization() =
    LegendaryCustomization(
        avatarGlow = avatarGlow,
        customBadge = customBadge,
        legendaryStyle = LegendaryStyle.valueById(id = styleId ?: ""),
        currentShoutout = currentShoutout,
        inLeaderboard = inLeaderboard,
    )
