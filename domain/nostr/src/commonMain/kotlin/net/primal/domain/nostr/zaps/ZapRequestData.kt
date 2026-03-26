package net.primal.domain.nostr.zaps

import net.primal.domain.nostr.NostrEvent

data class ZapRequestData(
    val zapperUserId: String,
    val target: ZapTarget,
    val zapAmountInSats: ULong,
    val zapComment: String,
    val userZapRequestEvent: NostrEvent,
)
