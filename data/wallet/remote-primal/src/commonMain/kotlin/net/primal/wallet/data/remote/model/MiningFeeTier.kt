package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.wallet.WalletAmount

@Serializable
data class MiningFeeTier(
    val id: String,
    @SerialName("_label") val label: String,
    @SerialName("_duration") val expirationInMin: Int,
    val estimatedDeliveryDurationInMin: Int,
    val estimatedFee: WalletAmount,
    val minimumAmount: WalletAmount? = null,
)
