package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.wallet.data.local.dao.nwc.NwcConnection as NostrWalletConnectionPO

fun NostrWalletConnectionPO.asDO() =
    NwcConnection(
        walletId = this.data.walletId,
        userId = this.data.userId,
        secretPubKey = this.data.secretPubKey,
        serviceKeyPair = NostrKeyPair(this.data.servicePrivateKey.decrypted, this.data.servicePubKey),
        relay = this.data.relay.decrypted,
        appName = this.data.appName.decrypted,
        dailyBudgetSats = this.data.dailyBudgetSats?.decrypted,
    )
