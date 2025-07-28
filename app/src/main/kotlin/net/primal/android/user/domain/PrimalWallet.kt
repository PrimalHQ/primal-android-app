package net.primal.android.user.domain

import kotlinx.serialization.Serializable
import net.primal.wallet.domain.WalletKycLevel

@Serializable
data class PrimalWallet(
    val kycLevel: WalletKycLevel = WalletKycLevel.None,
    val lightningAddress: String? = null,
)
