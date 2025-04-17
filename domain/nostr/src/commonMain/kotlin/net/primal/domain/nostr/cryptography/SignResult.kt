package net.primal.domain.nostr.cryptography

import net.primal.domain.nostr.NostrEvent

sealed class SignResult {
    data class Signed(val event: NostrEvent) : SignResult()
    data class Rejected(val error: SignatureException) : SignResult()
}
