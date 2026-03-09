package net.primal.data.remote.api.polls.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PollVotesRequestBody(
    @SerialName("event_id") val eventId: String,
    @SerialName("limit") val limit: Int = 20,
    @SerialName("since") val since: Long? = null,
    @SerialName("until") val until: Long? = null,
    @SerialName("offset") val offset: Int? = null,
)
