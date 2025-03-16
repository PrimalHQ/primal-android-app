package net.primal.android.messages.api.model

import net.primal.android.messages.domain.ConversationsSummary
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class ConversationsResponse(
    val conversationsSummary: ConversationsSummary?,
    val messages: List<NostrEvent>,
    val profileMetadata: List<NostrEvent>,
    val cdnResources: List<PrimalEvent>,
    val blossomServers: List<NostrEvent>,
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
)
