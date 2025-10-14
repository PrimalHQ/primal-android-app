package net.primal.wallet.data.repository.mappers.remote

import net.primal.core.utils.CurrencyConversionUtils.msatsToBtc
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.utils.LnInvoiceUtils
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.tsunami.model.TRANSFER_DIRECTION_INCOMING
import net.primal.tsunami.model.TsunamiTransfer
import net.primal.wallet.data.model.Transaction

internal fun TsunamiTransfer.mapAsWalletTransaction(
    userId: String,
    walletId: String,
    walletAddress: String?,
    zapRequest: NostrEvent?,
): Transaction {
    val zappedEntity = zapRequest?.toNostrEntity()
    val txType = if (this.direction.equals(TRANSFER_DIRECTION_INCOMING, ignoreCase = true)) {
        TxType.DEPOSIT
    } else {
        TxType.WITHDRAW
    }

    return Transaction.Tsunami(
        transactionId = this.id,
        walletId = walletId,
        type = txType,
        state = TxState.SUCCEEDED,
        createdAt = this.createdAt.secsSinceEpoch,
        updatedAt = this.updatedAt.secsSinceEpoch,
        completedAt = this.updatedAt.secsSinceEpoch,
        userId = userId,
        note = when (txType) {
            TxType.DEPOSIT ->
                this.userRequest.lightningReceiveRequest?.invoice
                    ?.encodedInvoice?.let(LnInvoiceUtils::getDescription)

            TxType.WITHDRAW ->
                this.userRequest.lightningSendRequest
                    ?.encodedInvoice?.let(LnInvoiceUtils::getDescription)
        },
        invoice = when (txType) {
            TxType.DEPOSIT -> this.userRequest.lightningReceiveRequest?.invoice?.encodedInvoice
            TxType.WITHDRAW -> this.userRequest.lightningSendRequest?.encodedInvoice
        },
        amountInBtc = this.totalValueSat.toBtc(),
        totalFeeInBtc = when (txType) {
            TxType.DEPOSIT -> null
            TxType.WITHDRAW -> this.userRequest.lightningSendRequest?.fee?.originalValue?.msatsToBtc()?.toString()
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
