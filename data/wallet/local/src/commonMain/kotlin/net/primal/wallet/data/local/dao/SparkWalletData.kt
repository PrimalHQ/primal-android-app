package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity(indices = [Index(value = ["userId"])])
data class SparkWalletData(
    @PrimaryKey
    val walletId: String,
    val userId: String,
    val seedWords: Encryptable<String>,
    val backedUp: Boolean = false,
    val primalTxsMigrated: Boolean? = null,
    val primalTxsMigratedUntil: Long? = null,
    val nwcAutoStart: Boolean = true,
)
