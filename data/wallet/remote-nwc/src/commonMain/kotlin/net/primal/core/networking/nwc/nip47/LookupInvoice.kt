package net.primal.core.networking.nwc.nip47

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.nostr.InvoiceType

@Serializable
data class LookupInvoiceParams(
    val invoice: String? = null,
    @SerialName("payment_hash") val paymentHash: String? = null,
)

@Serializable
data class LookupInvoiceResponsePayload(
    val type: InvoiceType,
    val invoice: String? = null,
    @SerialName("description_hash") val descriptionHash: String? = null,
    val description: String? = null,
    val preimage: String? = null,
    @SerialName("payment_hash") val paymentHash: String? = null,
    val amount: Long,
    @SerialName("fees_paid") val feesPaid: Long,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("expires_at") val expiresAt: Long? = null,
    @SerialName("settled_at") val settledAt: Long? = null,
    val metadata: Map<String, String>? = null,
)
