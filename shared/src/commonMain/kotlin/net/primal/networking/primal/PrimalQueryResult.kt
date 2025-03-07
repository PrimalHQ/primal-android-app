package net.primal.networking.primal

import net.primal.networking.model.NostrEvent
import net.primal.networking.model.NostrEventKind
import net.primal.networking.model.primal.PrimalEvent
import net.primal.networking.sockets.NostrIncomingMessage

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
