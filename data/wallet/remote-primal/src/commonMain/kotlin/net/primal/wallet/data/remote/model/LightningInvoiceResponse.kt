package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LightningInvoiceResponse(
    @SerialName("lnInvoice") val lnInvoice: String,
    @SerialName("description") val description: String? = null,
)
