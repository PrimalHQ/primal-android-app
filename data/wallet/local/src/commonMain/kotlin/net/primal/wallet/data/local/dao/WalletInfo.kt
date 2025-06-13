package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.wallet.WalletKycLevel
import net.primal.domain.wallet.WalletType

@Entity
data class WalletInfo(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val userId: String,
    val lightningAddress: String,
    val type: WalletType,
    val kycLevel: WalletKycLevel = WalletKycLevel.None,
)
