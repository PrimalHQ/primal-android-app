package net.primal.data.remote.api.settings.model

import kotlinx.serialization.Serializable
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent

@Serializable
data class GetStreamMuteListResponse(
    val streamMuteList: NostrEvent? = null,
    val metadataEvents: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
    val blossomServers: List<NostrEvent> = emptyList(),
)
