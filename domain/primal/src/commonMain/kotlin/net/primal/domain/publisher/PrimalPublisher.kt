package net.primal.domain.publisher

import net.primal.domain.nostr.NostrUnsignedEvent

interface PrimalPublisher {

    suspend fun signPublishImportNostrEvent(
        unsignedNostrEvent: NostrUnsignedEvent,
        outboxRelays: List<String> = emptyList(),
    ): PrimalPublishResult
}
