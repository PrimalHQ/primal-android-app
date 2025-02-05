package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPremiumLeaderboardItem(
    val index: Double,
    val pubkey: String,
    @SerialName("premium_since") val premiumSince: Long? = null,
)
