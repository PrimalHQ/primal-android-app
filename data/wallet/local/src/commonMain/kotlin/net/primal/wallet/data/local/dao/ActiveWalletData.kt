package net.primal.wallet.data.local.dao

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(indices = [Index(value = ["userId", "walletId"], unique = true)])
data class ActiveWalletData(
    @PrimaryKey
    val userId: String,
    val walletId: String,
)
