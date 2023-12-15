package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParsedLnUrlResponse(
    @SerialName("min_sendable") val minSendable: String?,
    @SerialName("max_sendable") val maxSendable: String?,
    @SerialName("description") val description: String?,
    @SerialName("target_pubkey") val targetPubkey: String?,
    @SerialName("target_lud16") val targetLud16: String?,
)
