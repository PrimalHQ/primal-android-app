package net.primal.data.repository.mappers.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalWordCount(
    @SerialName("event_id") val eventId: String,
    val words: Int,
)
