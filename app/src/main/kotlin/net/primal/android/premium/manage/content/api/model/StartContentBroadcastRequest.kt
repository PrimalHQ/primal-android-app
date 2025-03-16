package net.primal.android.premium.manage.content.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class StartContentBroadcastRequest(
    @SerialName("event_from_user") val eventFromUser: NostrEvent,
    val kinds: List<Int>? = null,
)
