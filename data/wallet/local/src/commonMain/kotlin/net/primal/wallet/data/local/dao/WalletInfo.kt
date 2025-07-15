package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import net.primal.domain.wallet.WalletType

@Entity
@OptIn(ExperimentalUuidApi::class)
data class WalletInfo(
    @PrimaryKey
    val walletId: String,
    val userId: String,
    val lightningAddress: String?,
    val type: WalletType,
    val balanceInBtc: Double? = null,
    val maxBalanceInBtc: Double? = null,
    val lastUpdatedAt: Long? = null,
)
