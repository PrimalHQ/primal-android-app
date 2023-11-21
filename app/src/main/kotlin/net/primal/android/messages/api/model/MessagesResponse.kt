package net.primal.android.messages.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

data class MessagesResponse(
    val messages: List<NostrEvent>,
    val paging: ContentPrimalPaging? = null,
    val profileMetadata: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
)
