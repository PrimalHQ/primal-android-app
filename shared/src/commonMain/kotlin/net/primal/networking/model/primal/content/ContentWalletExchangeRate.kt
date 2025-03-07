package net.primal.networking.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentWalletExchangeRate(
    @SerialName("target_currency") val targetCurrency: String,
    val rate: Double,
)
