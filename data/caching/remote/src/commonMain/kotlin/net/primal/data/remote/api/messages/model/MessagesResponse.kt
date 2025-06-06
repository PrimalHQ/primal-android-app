package net.primal.data.remote.api.messages.model

import net.primal.domain.common.ContentPrimalPaging
import net.primal.domain.common.PrimalEvent
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
