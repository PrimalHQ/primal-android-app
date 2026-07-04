package net.primal.wallet.data.local.dao

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class WalletInfo(
    @PrimaryKey
    val walletId: String,
    val type: WalletType,
    val balanceInBtc: Encryptable<Double>? = null,
    val maxBalanceInBtc: Encryptable<Double>? = null,
    val lastUpdatedAt: Long? = null,
)
