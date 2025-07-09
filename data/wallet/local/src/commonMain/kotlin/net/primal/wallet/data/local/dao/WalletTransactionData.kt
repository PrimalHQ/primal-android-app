package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.wallet.domain.SubWallet
import net.primal.wallet.domain.TxState
import net.primal.wallet.domain.TxType

@Entity
data class WalletTransactionData(
    @PrimaryKey
    val id: String,
    val walletLightningAddress: String,
    val type: TxType,
    val state: TxState,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val amountInBtc: Double,
    val amountInUsd: Double?,
    val isZap: Boolean,
    val isStorePurchase: Boolean,
    val userId: String,
    val userSubWallet: SubWallet,
    val userLightningAddress: String?,
    val otherUserId: String?,
    val otherLightningAddress: String?,
    val note: String?,
    val invoice: String?,
    val totalFeeInBtc: String?,
    val exchangeRate: String?,
    val onChainAddress: String?,
    val onChainTxId: String?,
    val zapNoteId: String?,
    val zapNoteAuthorId: String?,
    val zappedByUserId: String?,
)
