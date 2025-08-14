package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.encryption.Encryptable

@Entity
@OptIn(ExperimentalUuidApi::class)
data class WalletInfo(
    @PrimaryKey
    val walletId: String,
    val userId: Encryptable<String>,
    val lightningAddress: Encryptable<String>?,
    val type: WalletType,
    val balanceInBtc: Encryptable<Double>? = null,
    val maxBalanceInBtc: Encryptable<Double>? = null,
    val lastUpdatedAt: Long? = null,
)
