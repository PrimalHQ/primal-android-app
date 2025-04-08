package net.primal.domain.publisher

import kotlin.coroutines.cancellation.CancellationException
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.NostrPublishException

interface PrimalPublisher {

    @Throws(
        SigningKeyNotFoundException::class,
        SigningRejectedException::class,
        NostrPublishException::class,
        CancellationException::class,
    )
    suspend fun signPublishImportNostrEvent(
        unsignedNostrEvent: NostrUnsignedEvent,
        outboxRelays: List<String> = emptyList(),
    ): PrimalPublishResult
}
