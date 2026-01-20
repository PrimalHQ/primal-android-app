package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity(
    indices = [
        Index("connectionId"),
        Index("budgetDate"),
    ],
)
data class NwcBudgetReservationData(
    @PrimaryKey
    val reservationId: String,
    val connectionId: String,
    val requestId: String,
    val amountSats: Encryptable<Long>,
    val status: ReservationStatus,
    val budgetDate: String,
    val createdAt: Long,
    val expiresAt: Long,
    val updatedAt: Long,
)
