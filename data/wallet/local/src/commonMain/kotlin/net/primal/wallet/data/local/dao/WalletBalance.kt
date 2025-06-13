package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WalletBalance(
    @PrimaryKey
    val walletId: Long,
    val lastUpdatedAt: Long? = null,
    val balanceInBtc: String? = null,
    val maxBalanceInBtc: String? = null,
)
