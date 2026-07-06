package net.primal.wallet.data.local.dao.nwc

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity(
    indices = [
        Index("connectionId"),
        Index("budgetDate"),
    ],
)
data class NwcPaymentHoldData(
    @PrimaryKey
    val holdId: String,
    val connectionId: String,
    val requestId: String,
    val amountSats: Encryptable<Long>,
    val status: NwcPaymentHoldStatus,
    val budgetDate: String,
    val createdAt: Long,
    val expiresAt: Long,
    val updatedAt: Long,
)
