package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalEventStats(
    @SerialName("event_id") val eventId: String,
    val likes: Long,
    val replies: Long,
    val mentions: Long,
    val reposts: Long,
    val zaps: Long,
    @SerialName("satszapped") val satsZapped: Long,
    val score: Long,
    val score24h: Long,
)
