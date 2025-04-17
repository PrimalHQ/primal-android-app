package net.primal.domain.nostr.cryptography

import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

interface NostrEventSignatureHandler {

    suspend fun signNostrEvent(unsignedNostrEvent: NostrUnsignedEvent): SignResult

    fun verifySignature(nostrEvent: NostrEvent): Boolean
}
