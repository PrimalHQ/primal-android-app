package net.primal.wallet.data.local.dao

import androidx.room.Entity
import net.primal.shared.data.local.encryption.Encryptable

@Entity(primaryKeys = ["connectionId", "budgetDate"])
data class NwcDailySpendData(
    val connectionId: String,
    val budgetDate: String,
    val confirmedSpendSats: Encryptable<Long>,
    val lastUpdatedAt: Long,
)
