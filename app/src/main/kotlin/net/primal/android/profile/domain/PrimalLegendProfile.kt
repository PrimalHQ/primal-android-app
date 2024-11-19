package net.primal.android.profile.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.premium.legend.LegendaryProfile

@Serializable
data class PrimalLegendProfile(
    @SerialName("style") val styleId: String = LegendaryProfile.NO_CUSTOMIZATION.id,
    @SerialName("custom_badge") val customBadge: Boolean = false,
    @SerialName("avatar_glow") val avatarGlow: Boolean = false,
)
