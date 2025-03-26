package net.primal.domain.nostr.publisher

import net.primal.domain.nostr.NostrEvent

interface NostrEventPublisher {

    fun publishNostrEvent(
        userId: String,
        nostrEvent: NostrEvent,
        outboxRelays: List<String> = emptyList(),
    )
}
