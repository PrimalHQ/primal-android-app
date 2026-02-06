package net.primal.wallet.data.local.dao.nwc

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.wallet.nwc.model.NwcRequestState
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class NwcWalletRequestLog(
    @PrimaryKey
    val eventId: String,
    val connectionId: String,
    val walletId: String,
    val userId: String,
    val method: String,
    val requestPayload: Encryptable<String>,
    val responsePayload: Encryptable<String>?,
    val requestState: NwcRequestState,
    val errorCode: String?,
    val errorMessage: String?,
    val requestedAt: Long,
    val completedAt: Long?,
    val amountMsats: Long?,
)
