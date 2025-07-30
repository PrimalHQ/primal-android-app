package net.primal.domain.nostr.lightning.model

import kotlinx.serialization.Serializable

@Serializable
data class PayRequest(
    val tag: String,
    val callback: String,
    val minSendable: Long,
    val maxSendable: Long,
    val metadata: String,
    val nostrPubkey: String? = null,
    val allowsNostr: Boolean = false,
    val commentAllowed: Int = 0,
)
