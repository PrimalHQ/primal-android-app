package net.primal.android.wallet.zaps

import net.primal.domain.nostr.NostrEvent

data class ZapRequestData(
    val zapperUserId: String,
    val targetUserId: String,
    val lnUrlDecoded: String,
    val zapAmountInSats: ULong,
    val zapComment: String,
    val userZapRequestEvent: NostrEvent,
)
