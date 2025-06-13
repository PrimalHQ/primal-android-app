package net.primal.domain.wallet

import kotlinx.serialization.Serializable

@Serializable
data class WalletState(
    val balanceInBtc: String? = null,
    val lastUpdatedAt: Long? = null,
)
