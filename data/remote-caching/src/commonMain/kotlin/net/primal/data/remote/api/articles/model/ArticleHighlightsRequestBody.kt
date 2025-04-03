package net.primal.data.remote.api.articles.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleHighlightsRequestBody(
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("pubkey") val authorUserId: String? = null,
    @SerialName("identifier") val identifier: String? = null,
    @SerialName("kind") val kind: Int? = null,
    @SerialName("user_pubkey") val userId: String? = null,
)
