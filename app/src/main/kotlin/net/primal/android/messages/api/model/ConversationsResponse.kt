package net.primal.android.messages.api.model

import net.primal.android.messages.domain.ConversationsSummary
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

data class ConversationsResponse(
    val conversationsSummary: ConversationsSummary?,
    val messages: List<NostrEvent>,
    val profileMetadata: List<NostrEvent>,
    val cdnResources: List<PrimalEvent>,
)
