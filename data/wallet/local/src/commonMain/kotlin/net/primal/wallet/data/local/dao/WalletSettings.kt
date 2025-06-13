package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WalletSettings(
    @PrimaryKey
    val walletId: Long,
    val spamThresholdAmountInSats: Long = 1,
)
