package net.primal.wallet.data.local.dao

import androidx.room.Entity

@Entity(
    primaryKeys = ["walletId", "transactionId"],
)
data class WalletTransactionRemoteKey(
    val walletId: String,
    val transactionId: String,
    val sinceId: Long,
    val untilId: Long,
    val cachedAt: Long,
)
