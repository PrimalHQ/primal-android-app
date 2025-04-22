package net.primal.android.nostr.notary

import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.SignResult

class FakeNostrNotary(
    private val expectedSignedNostrEvent: NostrEvent,
) : NostrEventSignatureHandler {
    override suspend fun signNostrEvent(unsignedNostrEvent: NostrUnsignedEvent): SignResult {
        return SignResult.Signed(event = expectedSignedNostrEvent)
    }

    override fun verifySignature(nostrEvent: NostrEvent): Boolean {
        return true
    }
}
