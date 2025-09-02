package net.primal.domain.nostr.utils

import net.primal.core.utils.Result
import net.primal.core.utils.asSuccess
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.NostrEntity

fun String.asNostrEntity(): Result<NostrEntity> =
    when {
        isNEvent() || isNEventUri() -> Nip19TLV.parseUriAsNeventOrNull(neventUri = this)
        isNProfile() || isNProfileUri() -> Nip19TLV.parseUriAsNprofileOrNull(nprofileUri = this)
        isNAddr() || isNAddrUri() -> Nip19TLV.parseUriAsNaddrOrNull(naddrUri = this)
        else -> null
    }?.asSuccess()
        ?: Result.failure(IllegalArgumentException("couldn't parse $this as any nostr entity"))
