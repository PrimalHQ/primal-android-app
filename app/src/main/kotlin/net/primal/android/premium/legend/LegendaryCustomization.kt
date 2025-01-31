package net.primal.android.premium.legend

import net.primal.android.profile.domain.PrimalLegendProfile

data class LegendaryCustomization(
    val avatarGlow: Boolean = false,
    val customBadge: Boolean = false,
    val legendaryStyle: LegendaryStyle? = null,
    val legendSince: Long? = null,
    val currentShoutout: String? = null,
    val inLeaderboard: Boolean? = null,
)

fun PrimalLegendProfile.asLegendaryCustomization() =
    LegendaryCustomization(
        avatarGlow = avatarGlow,
        customBadge = customBadge,
        legendaryStyle = LegendaryStyle.valueById(id = styleId),
        legendSince = legendSince,
        currentShoutout = currentShoutout,
        inLeaderboard = inLeaderboard,
    )
