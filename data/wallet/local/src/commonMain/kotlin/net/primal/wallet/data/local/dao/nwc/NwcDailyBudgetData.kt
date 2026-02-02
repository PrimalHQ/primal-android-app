package net.primal.wallet.data.local.dao.nwc

import androidx.room.Entity
import net.primal.shared.data.local.encryption.Encryptable

@Entity(primaryKeys = ["connectionId", "budgetDate"])
data class NwcDailyBudgetData(
    val connectionId: String,
    val budgetDate: String,
    val confirmedSpendSats: Encryptable<Long>,
    val lastUpdatedAt: Long,
)
