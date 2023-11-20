package net.primal.android.settings.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

@Serializable
data class GetMuteListResponse(
    val muteList: NostrEvent? = null,
    val metadataEvents: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
)
