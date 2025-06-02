package net.primal.core.networking.nwc.nip47

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MakeInvoiceParams(
    val amount: Long,
    val description: String? = null,
    @SerialName("description_hash") val descriptionHash: String? = null,
    val expiry: Long? = null,
)

@Serializable
data class MakeInvoiceResponsePayload(
    val invoice: String,
    @SerialName("payment_hash") val paymentHash: String,
)
