package net.primal.domain.membership

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrimalLegendProfile(
    @SerialName("style") val styleId: String?,
    @SerialName("custom_badge") val customBadge: Boolean,
    @SerialName("avatar_glow") val avatarGlow: Boolean,
    @SerialName("in_leaderboard") val inLeaderboard: Boolean? = null,
    @SerialName("current_shoutout") val currentShoutout: String? = null,
)
