package net.primal.android.premium.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePrimalLegendProfileRequest(
    @SerialName("style") val styleId: String?,
    @SerialName("custom_badge") val customBadge: Boolean?,
    @SerialName("avatar_glow") val avatarGlow: Boolean?,
    @SerialName("in_leaderboard") val inLeaderboard: Boolean?,
    @SerialName("edited_shoutout") val editedShoutout: String?,
)
