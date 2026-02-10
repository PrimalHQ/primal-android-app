package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class ReceiveRequestData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val walletId: String,
    val userId: String,
    val type: ReceiveRequestType,
    val createdAt: Long,
    val fulfilledAt: Long? = null,
    val payload: Encryptable<String>,
    val amountInBtc: Encryptable<String>? = null,
)
