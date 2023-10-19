package net.primal.android.messages.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

data class MessagesResponse(
    val paging: ContentPrimalPaging?,
    val messages: List<NostrEvent>,
    val profileMetadata: List<NostrEvent>,
    val mediaResources: List<PrimalEvent>,
)
