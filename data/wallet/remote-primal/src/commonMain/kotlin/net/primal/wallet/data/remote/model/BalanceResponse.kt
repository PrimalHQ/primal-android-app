package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BalanceResponse(
    val amount: String,
    val currency: String,
    @SerialName("max_amount") val maxAmount: String? = null,
)
