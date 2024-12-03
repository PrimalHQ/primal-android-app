package net.primal.android.premium.legend

import net.primal.android.profile.domain.PrimalLegendProfile

data class LegendaryCustomization(
    val avatarGlow: Boolean = false,
    val customBadge: Boolean = false,
    val legendaryStyle: LegendaryStyle? = null,
)

fun PrimalLegendProfile.asLegendaryCustomization() =
    LegendaryCustomization(
        avatarGlow = avatarGlow,
        customBadge = customBadge,
        legendaryStyle = LegendaryStyle.valueById(id = styleId),
    )
