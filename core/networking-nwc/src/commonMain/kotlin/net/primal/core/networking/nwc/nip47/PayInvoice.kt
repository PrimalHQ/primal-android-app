package net.primal.core.networking.nwc.nip47

import kotlinx.serialization.Serializable

@Serializable
data class PayInvoiceParams(
    val invoice: String,
    val amount: Long? = null,
)

@Serializable
data class PayInvoiceResponsePayload(
    val preimage: String? = null,
)
