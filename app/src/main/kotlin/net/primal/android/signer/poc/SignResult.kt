package net.primal.android.signer.poc

import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.SignatureException

sealed class SignResult {
    data class Signed(val event: NostrEvent) : SignResult()
    data class Rejected(val error: SignatureException) : SignResult()
}
