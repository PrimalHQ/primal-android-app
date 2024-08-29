package net.primal.android.articles.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.primal.PrimalEvent

@Serializable
data class ArticleFeedsResponse(
    val articleFeeds: PrimalEvent,
)
