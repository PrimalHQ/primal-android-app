package net.primal.wallet.data.remote.nostr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentWalletExchangeRate(
    @SerialName("target_currency") val targetCurrency: String,
    val rate: Double,
)
