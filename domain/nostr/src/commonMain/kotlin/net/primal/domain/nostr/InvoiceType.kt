package net.primal.domain.nostr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class InvoiceType {
    @SerialName("incoming")
    Incoming,

    @SerialName("outgoing")
    Outgoing,
}
