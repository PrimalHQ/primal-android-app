package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalLeaderboardItem(
    val pubkey: String,
    @SerialName("donated_btc") val donatedBtc: Double,
)
