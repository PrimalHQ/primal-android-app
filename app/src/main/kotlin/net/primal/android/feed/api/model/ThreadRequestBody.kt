package net.primal.android.feed.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThreadRequestBody(
    @SerialName("event_id") val postId: String,
    @SerialName("user_pubkey") val userPubKey: String,
    @SerialName("limit") val limit: Int,
)
