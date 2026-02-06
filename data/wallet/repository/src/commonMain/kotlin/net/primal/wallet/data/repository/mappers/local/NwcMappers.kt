package net.primal.wallet.data.repository.mappers.local

import kotlin.time.Clock
import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.wallet.NwcInvoice
import net.primal.domain.wallet.NwcInvoiceState
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.nwc.NwcConnection as NostrWalletConnectionPO
import net.primal.wallet.data.local.dao.nwc.NwcInvoiceData

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

fun NwcInvoiceData.asDO() =
    NwcInvoice(
        invoice = this.invoice,
        paymentHash = this.paymentHash,
        walletId = this.walletId,
        connectionId = this.connectionId,
        description = this.description?.decrypted,
        descriptionHash = this.descriptionHash,
        amountMsats = this.amountMsats.decrypted,
        createdAt = this.createdAt,
        expiresAt = this.expiresAt,
        settledAt = this.settledAt,
        preimage = this.preimage?.decrypted,
        state = this.resolveInvoiceState(),
    )

fun NwcInvoice.asPO() =
    NwcInvoiceData(
        invoice = this.invoice,
        paymentHash = this.paymentHash,
        walletId = this.walletId,
        connectionId = this.connectionId,
        description = this.description?.asEncryptable(),
        descriptionHash = this.descriptionHash,
        amountMsats = this.amountMsats.asEncryptable(),
        createdAt = this.createdAt,
        expiresAt = this.expiresAt,
        settledAt = this.settledAt,
        preimage = this.preimage?.asEncryptable(),
    )

private fun NwcInvoiceData.resolveInvoiceState(): NwcInvoiceState {
    return when {
        expiresAt < Clock.System.now().epochSeconds -> NwcInvoiceState.EXPIRED
        settledAt != null -> NwcInvoiceState.SETTLED
        else -> NwcInvoiceState.PENDING
    }
}
