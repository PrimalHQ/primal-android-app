package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.wallet.WalletKycLevel

@Entity
data class PrimalWalletData(
    @PrimaryKey
    val walletId: String,
    val kycLevel: WalletKycLevel,
)
