package net.primal.domain.connections.nostr.model

import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.wallet.Wallet

data class NostrWalletConnection(
    val secretPubKey: String,
    val wallet: Wallet.Tsunami,
    val serviceKeyPair: NostrKeyPair,
    val relay: String,
    val appName: String,
    val dailyBudgetSats: Long?,
)
