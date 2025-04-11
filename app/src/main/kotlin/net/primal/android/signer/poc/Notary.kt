package net.primal.android.signer.poc

import net.primal.domain.nostr.NostrUnsignedEvent

interface Notary {
    suspend fun signEvent(unsignedEvent: NostrUnsignedEvent): SignResult
}
