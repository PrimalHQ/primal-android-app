package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.WalletType

@Entity
data class WalletTransactionData(
    @PrimaryKey
    val transactionId: String,
    val walletId: String,
    val walletType: WalletType,
    val type: TxType,
    val state: TxState,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val userId: String,
    val note: String?,
    val invoice: String?,
    val amountInBtc: Double,
    val totalFeeInBtc: String?,
)
