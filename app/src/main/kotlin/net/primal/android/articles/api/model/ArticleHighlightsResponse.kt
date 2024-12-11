package net.primal.android.articles.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent

@Serializable
data class ArticleHighlightsResponse(
    val highlights: List<NostrEvent>,
)
