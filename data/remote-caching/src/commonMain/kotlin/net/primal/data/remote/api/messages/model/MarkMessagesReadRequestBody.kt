package net.primal.data.remote.api.messages.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class MarkMessagesReadRequestBody(
    @SerialName("event_from_user") val authorization: NostrEvent,
    @SerialName("sender") val conversationUserId: String? = null,
)
