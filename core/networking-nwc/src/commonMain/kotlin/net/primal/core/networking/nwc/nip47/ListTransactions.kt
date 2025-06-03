package net.primal.core.networking.nwc.nip47

import kotlinx.serialization.Serializable

@Serializable
data class ListTransactionsParams(
    val from: Long? = null,
    val until: Long? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val unpaid: Boolean? = null,
    val type: String? = null,
)

@Serializable
data class ListTransactionsResponsePayload(
    val transactions: List<LookupInvoiceResponsePayload>,
)
