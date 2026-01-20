package net.primal.data.remote.api.broadcast.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class StartContentBroadcastRequest(
    @SerialName("event_from_user") val eventFromUser: NostrEvent,
    val kinds: List<Int>? = null,
)
