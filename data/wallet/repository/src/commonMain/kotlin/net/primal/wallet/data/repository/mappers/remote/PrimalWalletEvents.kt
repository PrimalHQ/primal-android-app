package net.primal.wallet.data.repository.mappers.remote

import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.model.Transaction
import net.primal.wallet.data.remote.nostr.ContentWalletTransaction

internal fun List<ContentWalletTransaction>.mapAsPrimalTransactions(
    walletId: String,
    userId: String,
    walletAddress: String?,
) = map { it.asPrimalTransactionDO(walletId, userId, walletAddress) }

internal fun ContentWalletTransaction.asPrimalTransactionDO(
    walletId: String,
    userId: String,
    walletAddress: String?,
): Transaction {
    val zapEvent = this.zapRequestRawJson.decodeFromJsonStringOrNull<NostrEvent>()
    val zappedEntity = zapEvent?.toNostrEntity()
    return Transaction.Primal(
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
        totalFeeInBtc = this.totalFeeInBtc,
        walletLightningAddress = walletAddress ?: "",
        amountInUsd = this.amountInUsd,
        isZap = this.isZap,
        isStorePurchase = this.isInAppPurchase,
        userSubWallet = this.selfSubWallet,
        userLightningAddress = walletAddress,
        otherUserId = this.otherPubkey,
        otherLightningAddress = this.otherLud16,
        exchangeRate = this.exchangeRate,
        onChainAddress = this.onChainAddress,
        onChainTxId = this.onChainTxId,
        zappedEntity = zappedEntity,
        zappedByUserId = zapEvent?.pubKey,
        otherUserProfile = null,
    )
}
