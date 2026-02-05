package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.wallet.NwcInvoice
import net.primal.domain.wallet.NwcInvoiceState as NwcInvoiceStateDO
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.nwc.NwcConnection as NostrWalletConnectionPO
import net.primal.wallet.data.local.dao.nwc.NwcInvoiceData
import net.primal.wallet.data.local.dao.nwc.NwcInvoiceState as NwcInvoiceStatePO

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
        state = this.state.asDO(),
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
        state = this.state.asPO(),
    )

private fun NwcInvoiceStatePO.asDO(): NwcInvoiceStateDO =
    when (this) {
        NwcInvoiceStatePO.PENDING -> NwcInvoiceStateDO.PENDING
        NwcInvoiceStatePO.SETTLED -> NwcInvoiceStateDO.SETTLED
        NwcInvoiceStatePO.EXPIRED -> NwcInvoiceStateDO.EXPIRED
    }

private fun NwcInvoiceStateDO.asPO(): NwcInvoiceStatePO =
    when (this) {
        NwcInvoiceStateDO.PENDING -> NwcInvoiceStatePO.PENDING
        NwcInvoiceStateDO.SETTLED -> NwcInvoiceStatePO.SETTLED
        NwcInvoiceStateDO.EXPIRED -> NwcInvoiceStatePO.EXPIRED
    }
