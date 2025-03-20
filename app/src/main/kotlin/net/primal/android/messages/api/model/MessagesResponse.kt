package net.primal.android.messages.api.model

import net.primal.data.remote.model.ContentPrimalPaging
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class MessagesResponse(
    val messages: List<NostrEvent>,
    val paging: ContentPrimalPaging? = null,
    val profileMetadata: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
    val blossomServers: List<NostrEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
)
