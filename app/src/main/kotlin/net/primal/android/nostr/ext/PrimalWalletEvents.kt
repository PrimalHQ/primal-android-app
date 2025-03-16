package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.nostr.model.primal.content.ContentWalletTransaction
import net.primal.android.wallet.db.WalletTransactionData
import net.primal.domain.nostr.NostrEvent

fun List<ContentWalletTransaction>.mapAsWalletTransactionPO(walletAddress: String) =
    map { it.asWalletTransactionPO(walletAddress) }

fun ContentWalletTransaction.asWalletTransactionPO(walletAddress: String): WalletTransactionData {
    val zapEvent = NostrJson.decodeFromStringOrNull<NostrEvent>(this.zapRequestRawJson)
    return WalletTransactionData(
        id = this.id,
        walletLightningAddress = walletAddress,
        type = this.type,
        state = this.state,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.completedAt,
        amountInBtc = this.amountInBtc,
        amountInUsd = this.amountInUsd,
        userId = this.selfPubkey,
        userSubWallet = this.selfSubWallet,
        userLightningAddress = this.selfLud16,
        otherUserId = this.otherPubkey,
        otherLightningAddress = this.otherLud16,
        note = this.note,
        isZap = this.isZap,
        isStorePurchase = this.isInAppPurchase,
        zapNoteId = zapEvent?.tags?.findFirstEventId(),
        zapNoteAuthorId = zapEvent?.tags?.findFirstProfileId(),
        zappedByUserId = zapEvent?.pubKey,
        invoice = this.invoice,
        totalFeeInBtc = this.totalFeeInBtc,
        exchangeRate = this.exchangeRate,
        onChainAddress = this.onChainAddress,
        onChainTxId = this.onChainTxId,
    )
}
