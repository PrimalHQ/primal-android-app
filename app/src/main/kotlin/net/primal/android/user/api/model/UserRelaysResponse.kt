package net.primal.android.user.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent

@Serializable
data class UserRelaysResponse(
    val followListEvent: NostrEvent? = null,
    val relayListMetadataEvent: NostrEvent? = null,
)
