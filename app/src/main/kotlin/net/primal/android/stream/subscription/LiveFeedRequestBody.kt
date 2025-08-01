package net.primal.android.stream.subscription

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveFeedRequestBody(
    val kind: Int,
    val pubkey: String,
    val identifier: String,
    @SerialName("user_pubkey") val userPubkey: String,
)
