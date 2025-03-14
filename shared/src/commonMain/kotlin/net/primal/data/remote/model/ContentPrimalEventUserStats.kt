package net.primal.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalEventUserStats(
    @SerialName("event_id") val eventId: String,
    @SerialName("replied") val replied: Boolean,
    @SerialName("liked") val liked: Boolean,
    @SerialName("reposted") val reposted: Boolean,
    @SerialName("zapped") val zapped: Boolean,
)
