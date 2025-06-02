package net.primal.core.networking.nwc.nip47

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LookupInvoiceParams(
    val invoice: String? = null,
    @SerialName("payment_hash") val paymentHash: String? = null,
) {
    init {
        require(invoice != null || paymentHash != null) {
            "Either invoice or payment_hash must be provided for lookup_invoice."
        }
    }
}

@Serializable
data class LookupInvoiceResponsePayload(
    val type: String? = null,
    val invoice: String? = null,
    @SerialName("description_hash") val descriptionHash: String? = null,
    val description: String? = null,
    val preimage: String? = null,
    @SerialName("payment_hash") val paymentHash: String,
    val amount: Long,
    val fees_paid: Long? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("expires_at") val expiresAt: Long? = null,
    @SerialName("settled_at") val settledAt: Long? = null,
    val metadata: Map<String, String>? = null,
)
