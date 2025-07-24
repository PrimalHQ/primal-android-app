package net.primal.wallet.data.local.dao

import androidx.room.Entity
import net.primal.shared.data.repository.paging.model.PrimalRemoteKey

@Entity(
    primaryKeys = ["transactionId", "walletId"],
)
data class WalletTransactionRemoteKey(
    val transactionId: String,
    val walletId: String,
    override val sinceId: Long,
    override val untilId: Long,
    override val cachedAt: Long,
) : PrimalRemoteKey(sinceId = sinceId, untilId = untilId, cachedAt = cachedAt)
