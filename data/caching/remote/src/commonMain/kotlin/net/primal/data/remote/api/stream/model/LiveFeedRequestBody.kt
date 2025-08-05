package net.primal.data.remote.api.stream.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveFeedRequestBody(
    val kind: Int,
    val pubkey: String,
    val identifier: String,
    @SerialName("user_pubkey") val userPubkey: String,
)
