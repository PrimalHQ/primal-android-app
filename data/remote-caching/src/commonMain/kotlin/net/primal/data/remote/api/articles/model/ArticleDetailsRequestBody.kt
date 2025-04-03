package net.primal.data.remote.api.articles.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleDetailsRequestBody(
    @SerialName("user_pubkey") val userId: String,
    @SerialName("pubkey") val authorUserId: String,
    val identifier: String,
    val kind: Int,
    val limit: Int,
)
