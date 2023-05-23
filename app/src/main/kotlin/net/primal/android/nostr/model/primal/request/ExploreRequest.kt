package net.primal.android.nostr.model.primal.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExploreRequest(
    val timeframe: String,
    val scope: String,
    @SerialName("pubkey") val pubKey: String,
)
