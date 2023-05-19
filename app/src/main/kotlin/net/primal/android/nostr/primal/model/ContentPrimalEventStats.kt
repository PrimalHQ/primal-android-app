package net.primal.android.nostr.primal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalEventStats(
    @SerialName("event_id") val eventId: String,
    val likes: Int,
    val replies: Int,
    val mentions: Int,
    val reposts: Int,
    val zaps: Int,
    @SerialName("satszapped") val satsZapped: Int,
    val score: Int,
    val score24h: Int,
)
