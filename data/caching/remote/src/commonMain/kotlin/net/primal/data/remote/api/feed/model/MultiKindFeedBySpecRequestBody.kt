package net.primal.data.remote.api.feed.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MultiKindFeedBySpecRequestBody(
    @SerialName("spec") val spec: String,
    @SerialName("user_pubkey") val userPubKey: String,
    @SerialName("kinds") val kinds: List<Int>,
    @SerialName("notes") val notes: String? = null,
    @SerialName("limit") val limit: Int? = null,
    @SerialName("until") val until: Long? = null,
    @SerialName("since") val since: Long? = null,
    @SerialName("order") val order: String? = null,
)
