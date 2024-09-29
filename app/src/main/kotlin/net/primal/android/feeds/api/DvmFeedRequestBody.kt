package net.primal.android.feeds.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DvmFeedRequestBody(
    @SerialName("dvm_pubkey") val dvmPubkey: String,
    @SerialName("dvm_id") val dvmId: String,
    @SerialName("user_pubkey") val pubkey: String?,
)
