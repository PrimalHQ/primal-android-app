package net.primal.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentDvmFeedFollowsAction(
    @SerialName("event_id") val eventId: String,
    @SerialName("dvm_pubkey") val dvmPubkey: String,
    @SerialName("dvm_id") val dvmId: String,
    @SerialName("users") val userIds: List<String>,
)
