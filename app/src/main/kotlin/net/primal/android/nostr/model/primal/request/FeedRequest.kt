package net.primal.android.nostr.model.primal.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedRequest(
    @SerialName("pubkey") val pubKey: String,
    @SerialName("user_pubkey") val userPubKey: String,
)
