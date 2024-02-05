package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParsedLnUrlResponse(
    @SerialName("min_sendable") val minSendable: String? = null,
    @SerialName("max_sendable") val maxSendable: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("target_pubkey") val targetPubkey: String? = null,
    @SerialName("target_lud16") val targetLud16: String? = null,
)
