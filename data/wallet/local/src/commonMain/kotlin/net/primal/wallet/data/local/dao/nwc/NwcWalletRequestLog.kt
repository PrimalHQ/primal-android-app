package net.primal.wallet.data.local.dao.nwc

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class NwcWalletRequestLog(
    @PrimaryKey
    val eventId: String,
    val connectionId: Encryptable<String>,
    val walletId: Encryptable<String>,
    val userId: Encryptable<String>,
    val method: Encryptable<String>,
    val requestPayload: Encryptable<String>,
    val responsePayload: Encryptable<String>?,
    val requestState: Encryptable<String>,
    val errorCode: Encryptable<String>?,
    val errorMessage: Encryptable<String>?,
    val requestedAt: Long,
    val completedAt: Long?,
    val amountMsats: Encryptable<Long>?,
)
