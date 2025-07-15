package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["userId", "walletId"], unique = true)])
data class ActiveWalletData(
    @PrimaryKey
    val userId: String,
    val walletId: String,
)
