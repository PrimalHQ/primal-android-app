package net.primal.android.user.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

@Serializable
data class UserContactsResponse(
    val contactsEvent: NostrEvent? = null,
    val contactsMetadata: List<NostrEvent> = emptyList(),
    val userScores: PrimalEvent? = null,
    val cdnResources: List<PrimalEvent> = emptyList(),
)
