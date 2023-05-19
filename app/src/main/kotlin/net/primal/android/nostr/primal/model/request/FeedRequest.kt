package net.primal.android.nostr.primal.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedRequest(
    @SerialName("pubkey") val pubKey: String,
    @SerialName("user_pubkey") val userPubKey: String,
)
