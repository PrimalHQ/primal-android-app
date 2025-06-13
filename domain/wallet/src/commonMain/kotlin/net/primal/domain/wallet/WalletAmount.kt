package net.primal.domain.wallet

import kotlinx.serialization.Serializable

@Serializable
data class WalletAmount(
    val amount: String,
    val currency: String,
)
