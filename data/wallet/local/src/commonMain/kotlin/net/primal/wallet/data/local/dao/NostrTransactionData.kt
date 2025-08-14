package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class NostrTransactionData(
    @PrimaryKey
    val transactionId: String,
    val preimage: Encryptable<String>?,
    val descriptionHash: Encryptable<String>?,
    val paymentHash: Encryptable<String>?,
    val metadata: Encryptable<String>?,
)
