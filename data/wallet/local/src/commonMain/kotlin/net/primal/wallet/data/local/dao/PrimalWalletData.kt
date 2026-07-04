package net.primal.wallet.data.local.dao

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import net.primal.domain.wallet.WalletKycLevel

@Entity
data class PrimalWalletData(
    @PrimaryKey
    val walletId: String,
    val kycLevel: WalletKycLevel,
)
