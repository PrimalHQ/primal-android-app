package net.primal.data.remote.api.polls.model

import net.primal.domain.common.ContentPrimalPaging
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class PollVotesResponse(
    val profiles: List<NostrEvent> = emptyList(),
    val pollResponses: List<NostrEvent> = emptyList(),
    val zaps: List<NostrEvent> = emptyList(),
    val pollStats: List<PrimalEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
    val blossomServers: List<NostrEvent> = emptyList(),
    val referencedEvents: List<PrimalEvent> = emptyList(),
    val paging: ContentPrimalPaging? = null,
)
