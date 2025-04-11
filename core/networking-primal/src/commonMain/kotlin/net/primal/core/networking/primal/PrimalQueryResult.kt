package net.primal.core.networking.primal

import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

data class PrimalQueryResult(
    val terminationMessage: NostrIncomingMessage,
    val nostrEvents: List<NostrEvent> = emptyList(),
    val primalEvents: List<PrimalEvent> = emptyList(),
) {
    fun findNostrEvent(kind: NostrEventKind) = nostrEvents.find { kind.value == it.kind }

    fun findPrimalEvent(kind: NostrEventKind) = primalEvents.find { kind.value == it.kind }

    fun filterNostrEvents(kind: NostrEventKind) = nostrEvents.filter { kind.value == it.kind }

    fun filterPrimalEvents(kind: NostrEventKind) = primalEvents.filter { kind.value == it.kind }
}
