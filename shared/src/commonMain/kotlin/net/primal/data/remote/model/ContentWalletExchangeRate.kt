package net.primal.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentWalletExchangeRate(
    @SerialName("target_currency") val targetCurrency: String,
    val rate: Double,
)
