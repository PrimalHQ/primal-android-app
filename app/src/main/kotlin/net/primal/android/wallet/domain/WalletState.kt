package net.primal.android.wallet.domain

import kotlinx.serialization.Serializable

@Serializable
data class WalletState(
    val balanceInBtc: String? = null,
    val lastUpdatedAt: Long? = null,
)
