package net.primal.wallet.data.repository.mappers.remote

import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.remote.nostr.ContentWalletTransaction

internal fun List<ContentWalletTransaction>.mapAsPrimalTransactions(
    walletId: String,
    userId: String,
): List<Transaction> = map { it.asPrimalTransaction(walletId, userId) }

internal fun ContentWalletTransaction.asPrimalTransaction(walletId: String, userId: String): Transaction {
    val zapEvent = this.zapRequestRawJson.decodeFromJsonStringOrNull<NostrEvent>()
    val zappedEntity = zapEvent?.toNostrEntity()

    // Determine transaction type based on content
    val isZap = this.isZap && zappedEntity != null
    val isStorePurchase = this.isInAppPurchase
    val isOnChain = this.onChainAddress != null

    // Create the appropriate domain transaction based on kind
    return when {
        isStorePurchase -> Transaction.StorePurchase(
            transactionId = this.id,
            walletId = walletId,
            walletType = WalletType.PRIMAL,
            type = this.type,
            state = this.state,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            completedAt = this.completedAt,
            userId = userId,
            note = this.note,
            invoice = this.invoice,
            amountInBtc = this.amountInBtc,
            amountInUsd = this.amountInUsd,
            exchangeRate = this.exchangeRate,
            totalFeeInBtc = this.totalFeeInBtc,
        )
        isZap -> Transaction.Zap(
            transactionId = this.id,
            walletId = walletId,
            walletType = WalletType.PRIMAL,
            type = this.type,
            state = this.state,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            completedAt = this.completedAt,
            userId = userId,
            note = this.note,
            invoice = this.invoice,
            amountInBtc = this.amountInBtc,
            amountInUsd = this.amountInUsd,
            exchangeRate = this.exchangeRate,
            totalFeeInBtc = this.totalFeeInBtc,
            zappedEntity = zappedEntity!!,
            otherUserId = this.otherPubkey,
            otherLightningAddress = this.otherLud16,
            zappedByUserId = zapEvent?.pubKey,
            otherUserProfile = null,
            preimage = null,
            paymentHash = null,
        )
        isOnChain -> Transaction.OnChain(
            transactionId = this.id,
            walletId = walletId,
            walletType = WalletType.PRIMAL,
            type = this.type,
            state = this.state,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            completedAt = this.completedAt,
            userId = userId,
            note = this.note,
            invoice = this.invoice,
            amountInBtc = this.amountInBtc,
            amountInUsd = this.amountInUsd,
            exchangeRate = this.exchangeRate,
            totalFeeInBtc = this.totalFeeInBtc,
            onChainTxId = this.onChainTxId,
            onChainAddress = this.onChainAddress,
        )
        else -> Transaction.Lightning(
            transactionId = this.id,
            walletId = walletId,
            walletType = WalletType.PRIMAL,
            type = this.type,
            state = this.state,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            completedAt = this.completedAt,
            userId = userId,
            note = this.note,
            invoice = this.invoice,
            amountInBtc = this.amountInBtc,
            amountInUsd = this.amountInUsd,
            exchangeRate = this.exchangeRate,
            totalFeeInBtc = this.totalFeeInBtc,
            otherUserId = this.otherPubkey,
            otherLightningAddress = this.otherLud16,
            otherUserProfile = null,
            preimage = null,
            paymentHash = null,
        )
    }
}
