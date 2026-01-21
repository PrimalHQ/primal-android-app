package net.primal.domain.connections.nostr.model

import net.primal.domain.nostr.cryptography.NostrKeyPair

data class NwcConnection(
    val walletId: String,
    val userId: String,
    val secretPubKey: String,
    val serviceKeyPair: NostrKeyPair,
    val relay: String,
    val appName: String,
    val dailyBudgetSats: Long?,
)
