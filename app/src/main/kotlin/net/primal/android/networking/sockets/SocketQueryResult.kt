package net.primal.android.networking.sockets

import net.primal.android.networking.sockets.model.IncomingMessage
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

data class SocketQueryResult(
    val terminationMessage: IncomingMessage,
    val nostrEvents: List<NostrEvent> = emptyList(),
    val primalEvents: List<PrimalEvent> = emptyList(),
)
