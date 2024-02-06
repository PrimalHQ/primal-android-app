package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnChainAddressResponse(
    @SerialName("onchainAddress") val onChainAddress: String,
)
