package net.primal.wallet.data.local.dao

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class SparkWalletData(
    @PrimaryKey
    val walletId: String,
    val seedWords: Encryptable<String>,
    val backedUp: Boolean = false,
    val primalTxsMigrated: Boolean? = null,
    val primalTxsMigratedUntil: Long? = null,
)
