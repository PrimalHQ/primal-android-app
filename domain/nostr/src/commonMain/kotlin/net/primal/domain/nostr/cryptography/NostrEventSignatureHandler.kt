package net.primal.domain.nostr.cryptography

import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

interface NostrEventSignatureHandler {

    fun signNostrEvent(unsignedNostrEvent: NostrUnsignedEvent): NostrEvent

    fun verifySignature(nostrEvent: NostrEvent): Boolean
}
