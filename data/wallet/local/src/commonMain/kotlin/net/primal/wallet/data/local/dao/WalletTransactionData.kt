package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class WalletTransactionData(
    @PrimaryKey
    val transactionId: String,
    val walletId: String,
    val walletType: WalletType,
    val type: TxType,
    val state: TxState,
    val createdAt: Encryptable<Long>,
    val updatedAt: Long,
    val completedAt: Encryptable<Long>?,
    val userId: Encryptable<String>,
    val note: Encryptable<String>?,
    val invoice: Encryptable<String>?,
    val amountInBtc: Encryptable<Double>,
    val totalFeeInBtc: Encryptable<String>?,
    val otherUserId: Encryptable<String>?,
    val zappedEntity: Encryptable<String>?,
    val zappedByUserId: Encryptable<String>?,
)
