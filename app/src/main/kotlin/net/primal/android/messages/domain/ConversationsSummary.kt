package net.primal.android.messages.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ConversationsSummary(
    val summaryPerParticipantId: Map<String, ConversationSummary> = mapOf(),
)

@Serializable
data class ConversationSummary(
    @SerialName("latest_event_id") val lastMessageId: String,
    @SerialName("latest_at") val lastMessageAt: Long,
    @SerialName("cnt") val count: Int,
)
