package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserWalletPreferences(
    @PrimaryKey
    val userId: String,
    val nwcAutoStart: Boolean = true,
)
