package net.primal.android.networking.sockets

import net.primal.android.networking.sockets.model.IncomingMessage
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.NostrPrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

data class SocketQueryResult(
    val terminationMessage: IncomingMessage,
    val nostrEvents: List<NostrEvent> = emptyList(),
    val primalEvents: List<NostrPrimalEvent> = emptyList(),
    val pagingEvent: ContentPrimalPaging? = null,
)
