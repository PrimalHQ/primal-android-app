package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NostrTransactionData(
    @PrimaryKey
    val transactionId: String,
    val preimage: String?,
    val descriptionHash: String?,
    val paymentHash: String?,
    val metadata: String?,
)
