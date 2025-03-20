package net.primal.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalEventStats(
    @SerialName("event_id") val eventId: String,
    val likes: Long = 0,
    val replies: Long = 0,
    val mentions: Long = 0,
    val reposts: Long = 0,
    val zaps: Long = 0,
    @SerialName("satszapped") val satsZapped: Long = 0,
    val score: Long = 0,
    val score24h: Long = 0,
)
