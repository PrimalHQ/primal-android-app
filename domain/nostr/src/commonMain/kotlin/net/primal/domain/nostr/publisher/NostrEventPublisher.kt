package net.primal.domain.nostr.publisher

import kotlin.coroutines.cancellation.CancellationException
import net.primal.domain.nostr.NostrEvent

interface NostrEventPublisher {

    @Throws(NostrPublishException::class, CancellationException::class)
    suspend fun publishNostrEvent(
        userId: String,
        nostrEvent: NostrEvent,
        outboxRelays: List<String> = emptyList(),
    )
}
