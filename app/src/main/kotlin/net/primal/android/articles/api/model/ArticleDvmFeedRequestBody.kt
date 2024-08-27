package net.primal.android.articles.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleDvmFeedRequestBody(
    @SerialName("dvm_pubkey") val dvmPubkey: String,
    @SerialName("dvm_id") val dvmId: String,
    @SerialName("user_pubkey") val userPubKey: String,
)
