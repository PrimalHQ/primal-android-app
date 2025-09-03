package net.primal.data.remote.api.events.model

import kotlinx.serialization.Serializable

@Serializable
data class InvoicesToZapReceiptsRequest(
    val invoices: List<String>,
)
