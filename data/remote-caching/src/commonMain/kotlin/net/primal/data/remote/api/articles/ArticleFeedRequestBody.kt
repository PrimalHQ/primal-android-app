package net.primal.data.remote.api.articles

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleFeedRequestBody(
    @SerialName("spec") val spec: String,
    @SerialName("user_pubkey") val userId: String? = null,
    @SerialName("limit") val limit: Int? = null,
    @SerialName("until") val until: Long? = null,
    @SerialName("since") val since: Long? = null,
)
