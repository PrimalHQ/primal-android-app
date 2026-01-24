package net.primal.wallet.data.repository.mappers.remote

import net.primal.core.utils.CurrencyConversionUtils.msatsToBtc
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.utils.LnInvoiceUtils
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.WalletType
import net.primal.tsunami.model.Transfer
import net.primal.tsunami.model.TransferDirection

internal fun Transfer.mapAsWalletTransaction(
    userId: String,
    walletId: String,
    walletAddress: String?,
    zapRequest: NostrEvent?,
): Transaction {
    val zappedEntity = zapRequest?.toNostrEntity()
    val txType = when (this.direction) {
        TransferDirection.Incoming -> TxType.DEPOSIT
        TransferDirection.Outgoing -> TxType.WITHDRAW
    }

    val invoice = when (txType) {
        TxType.DEPOSIT -> this.lightningReceiveRequest?.encodedInvoice
        TxType.WITHDRAW -> this.lightningSendRequest?.encodedInvoice
    }
    val note = when (txType) {
        TxType.DEPOSIT -> this.lightningReceiveRequest?.encodedInvoice?.let(LnInvoiceUtils::getDescription)
        TxType.WITHDRAW -> this.lightningSendRequest?.encodedInvoice?.let(LnInvoiceUtils::getDescription)
    }
    val totalFeeInBtc = when (txType) {
        TxType.DEPOSIT -> null
        TxType.WITHDRAW -> this.lightningSendRequest?.feeInMillisats?.msatsToBtc()?.toString()
    }
    val otherUserId = when (txType) {
        TxType.DEPOSIT -> zapRequest?.pubKey
        TxType.WITHDRAW -> zapRequest?.tags?.findFirstProfileId()
    }

    return if (zappedEntity != null) {
        Transaction.Zap(
            transactionId = this.id,
            walletId = walletId,
            walletType = WalletType.TSUNAMI,
            type = txType,
            state = TxState.SUCCEEDED,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            completedAt = this.updatedAt,
            userId = userId,
            note = note,
            invoice = invoice,
            amountInBtc = this.totalAmountInSats.toBtc(),
            amountInUsd = null,
            exchangeRate = null,
            totalFeeInBtc = totalFeeInBtc,
            zappedEntity = zappedEntity,
            otherUserId = otherUserId,
            otherLightningAddress = null,
            zappedByUserId = zapRequest?.pubKey,
            otherUserProfile = null,
            preimage = null,
            paymentHash = null,
        )
    } else {
        Transaction.Lightning(
            transactionId = this.id,
            walletId = walletId,
            walletType = WalletType.TSUNAMI,
            type = txType,
            state = TxState.SUCCEEDED,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            completedAt = this.updatedAt,
            userId = userId,
            note = note,
            invoice = invoice,
            amountInBtc = this.totalAmountInSats.toBtc(),
            amountInUsd = null,
            exchangeRate = null,
            totalFeeInBtc = totalFeeInBtc,
            otherUserId = otherUserId,
            otherLightningAddress = null,
            otherUserProfile = null,
            preimage = null,
            paymentHash = null,
        )
    }
}
