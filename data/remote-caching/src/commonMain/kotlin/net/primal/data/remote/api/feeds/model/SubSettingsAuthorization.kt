package net.primal.data.remote.api.feeds.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class SubSettingsAuthorization(
    @SerialName("event_from_user") val event: NostrEvent,
)
