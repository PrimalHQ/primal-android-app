package net.primal.wallet.data.repository.mappers.remote

import net.primal.core.utils.CurrencyConversionUtils.msatsToBtc
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.utils.LnInvoiceUtils
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.tsunami.model.Transfer
import net.primal.tsunami.model.TransferDirection
import net.primal.wallet.data.model.Transaction

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

    return Transaction.Tsunami(
        transactionId = this.id,
        walletId = walletId,
        type = txType,
        state = TxState.SUCCEEDED,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.updatedAt,
        userId = userId,
        note = when (txType) {
            TxType.DEPOSIT -> this.lightningReceiveRequest?.encodedInvoice?.let(LnInvoiceUtils::getDescription)
            TxType.WITHDRAW -> this.lightningSendRequest?.encodedInvoice?.let(LnInvoiceUtils::getDescription)
        },
        invoice = when (txType) {
            TxType.DEPOSIT -> this.lightningReceiveRequest?.encodedInvoice
            TxType.WITHDRAW -> this.lightningSendRequest?.encodedInvoice
        },
        amountInBtc = this.totalAmountInSats.toBtc(),
        totalFeeInBtc = when (txType) {
            TxType.DEPOSIT -> null
            TxType.WITHDRAW -> this.lightningSendRequest?.feeInMillisats?.msatsToBtc()
                ?.toString()
        },
        otherUserId = when (txType) {
            TxType.DEPOSIT -> zapRequest?.pubKey
            TxType.WITHDRAW -> zapRequest?.tags?.findFirstProfileId()
        },
        zappedByUserId = zapRequest?.pubKey,
        zappedEntity = zappedEntity,
        otherUserProfile = null,
    )
}
