package net.primal.android.settings.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class AppSpecificDataRequest(
    @SerialName("event_from_user") val eventFromUser: NostrEvent,
)
