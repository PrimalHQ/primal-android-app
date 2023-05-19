package net.primal.android.nostr.primal.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExploreRequest(
    val timeframe: String,
    val scope: String,
    @SerialName("pubkey") val pubKey: String,
)
