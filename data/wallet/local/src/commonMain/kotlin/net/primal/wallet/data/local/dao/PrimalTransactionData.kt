package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.wallet.SubWallet

@Entity
data class PrimalTransactionData(
    @PrimaryKey
    val transactionId: String,
    val walletLightningAddress: String,
    val amountInUsd: Double?,
    val isZap: Boolean,
    val isStorePurchase: Boolean,
    val userSubWallet: SubWallet,
    val userLightningAddress: String?,
    val otherUserId: String?,
    val otherLightningAddress: String?,
    val exchangeRate: String?,
    val onChainAddress: String?,
    val onChainTxId: String?,
    val zapNoteId: String?,
    val zapNoteAuthorId: String?,
    val zappedByUserId: String?,
)
