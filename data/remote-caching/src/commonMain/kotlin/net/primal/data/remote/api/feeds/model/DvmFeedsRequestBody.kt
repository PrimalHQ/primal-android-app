package net.primal.data.remote.api.feeds.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DvmFeedsRequestBody(
    @SerialName("kind") val specKind: String?,
    @SerialName("user_pubkey") val pubkey: String?,
)
