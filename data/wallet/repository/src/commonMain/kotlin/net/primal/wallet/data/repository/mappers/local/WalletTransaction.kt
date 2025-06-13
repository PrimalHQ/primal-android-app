package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.wallet.WalletTransaction as WalletTransactionDO
import net.primal.wallet.data.local.dao.WalletTransactionData

internal fun WalletTransactionData.asWalletTransactionDO(): WalletTransactionDO {
    return WalletTransactionDO(
        id = this.id,
        walletLightningAddress = this.walletLightningAddress,
        type = this.type,
        state = this.state,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.completedAt,
        amountInBtc = this.amountInBtc,
        amountInUsd = this.amountInUsd,
        isZap = this.isZap,
        isStorePurchase = this.isStorePurchase,
        userId = this.userId,
        userSubWallet = this.userSubWallet,
        userLightningAddress = this.userLightningAddress,
        otherUserId = this.otherUserId,
        otherLightningAddress = this.otherLightningAddress,
        note = this.note,
        invoice = this.invoice,
        totalFeeInBtc = this.totalFeeInBtc,
        exchangeRate = this.exchangeRate,
        onChainAddress = this.onChainAddress,
        onChainTxId = this.onChainTxId,
        zapNoteId = this.zapNoteId,
        zapNoteAuthorId = this.zapNoteAuthorId,
        zappedByUserId = this.zappedByUserId,
    )
}
