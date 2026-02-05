package net.primal.wallet.data.local.dao.nwc

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity(
    indices = [
        Index("walletId"),
        Index("connectionId"),
        Index("paymentHash"),
    ],
)
data class NwcInvoiceData(
    @PrimaryKey
    val invoice: String,
    val paymentHash: String?,
    val walletId: String,
    val connectionId: String,
    val description: Encryptable<String>?,
    val descriptionHash: String?,
    val amountMsats: Encryptable<Long>,
    val createdAt: Long,
    val expiresAt: Long,
    val settledAt: Long?,
    val preimage: Encryptable<String>?,
    val state: NwcInvoiceState,
)
