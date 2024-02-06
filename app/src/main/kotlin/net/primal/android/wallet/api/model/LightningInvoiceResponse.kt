package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LightningInvoiceResponse(
    @SerialName("lnInvoice") val lnInvoice: String?,
    @SerialName("description") val description: String? = null,
)
