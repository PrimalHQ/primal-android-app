package net.primal.android.nostr.model.primal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrimalResourceVariant(
    @SerialName("mt") val mimeType: String,
    @SerialName("s") val size: String,
    @SerialName("w") val width: Int,
    @SerialName("h") val height: Int,
    @SerialName("media_url") val mediaUrl: String,
)
