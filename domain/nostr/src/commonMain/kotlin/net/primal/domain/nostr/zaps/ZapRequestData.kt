package net.primal.domain.nostr.zaps

import net.primal.domain.nostr.NostrEvent

data class ZapRequestData(
    val zapperUserId: String,
    val recipientUserId: String,
    val recipientLnUrlDecoded: String,
    val zapAmountInSats: ULong,
    val zapComment: String,
    val userZapRequestEvent: NostrEvent,
)
