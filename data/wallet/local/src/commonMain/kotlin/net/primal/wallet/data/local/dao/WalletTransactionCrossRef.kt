package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["walletId", "transactionId"], unique = true)])
data class WalletTransactionCrossRef(
    @PrimaryKey(autoGenerate = true)
    val position: Long = 0,
    val walletId: String,
    val transactionId: String,
)
