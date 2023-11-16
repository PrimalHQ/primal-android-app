package net.primal.android.networking.primal

import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent

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
