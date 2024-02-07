package net.primal.android.wallet.domain

import kotlinx.serialization.Serializable

@Serializable
data class WalletAmount(
    val amount: String,
    val currency: String,
)
