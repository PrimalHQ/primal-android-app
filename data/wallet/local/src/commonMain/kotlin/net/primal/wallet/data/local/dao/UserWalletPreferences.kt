package net.primal.wallet.data.local.dao

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class UserWalletPreferences(
    @PrimaryKey
    val userId: String,
    val nwcAutoStart: Boolean = true,
)
