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
    val type: String,
    val invoice: String? = null,
    val description: String? = null,
    @SerialName("description_hash") val descriptionHash: String? = null,
    val preimage: String? = null,
    @SerialName("payment_hash") val paymentHash: String,
    val amount: Long,
    @SerialName("fees_paid") val feesPaid: Long? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("expires_at") val expiresAt: Long? = null,
    val metadata: Map<String, String>? = null,
)
