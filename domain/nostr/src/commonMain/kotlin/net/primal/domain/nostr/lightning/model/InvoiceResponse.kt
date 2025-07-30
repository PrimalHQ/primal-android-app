package net.primal.domain.nostr.lightning.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InvoiceResponse(
    @SerialName("pr") val invoice: String,
    val routes: List<String> = emptyList(),
)
