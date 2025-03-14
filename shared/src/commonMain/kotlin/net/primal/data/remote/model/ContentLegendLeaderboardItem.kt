package net.primal.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentLegendLeaderboardItem(
    val pubkey: String,
    @SerialName("donated_btc") val donatedSats: String,
)
