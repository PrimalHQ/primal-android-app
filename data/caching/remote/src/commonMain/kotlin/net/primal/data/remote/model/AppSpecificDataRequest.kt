package net.primal.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppSpecificDataRequest(
    @SerialName("event_from_user") val eventFromUser: net.primal.domain.nostr.NostrEvent,
)
