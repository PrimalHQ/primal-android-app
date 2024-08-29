package net.primal.android.feeds.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent

@Serializable
data class SubSettingsAuthorization(
    @SerialName("event_from_user") val event: NostrEvent,
)
