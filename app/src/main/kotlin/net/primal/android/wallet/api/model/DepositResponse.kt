package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DepositResponse(
    @SerialName("lnInvoice") val lnInvoice: String?,
    @SerialName("description") val description: String? = null,
    @SerialName("onchainAddress") val onChainAddress: String? = null,
)
