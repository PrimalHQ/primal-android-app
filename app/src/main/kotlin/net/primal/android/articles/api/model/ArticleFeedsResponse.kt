package net.primal.android.articles.api.model

import kotlinx.serialization.Serializable
import net.primal.domain.PrimalEvent

@Serializable
data class ArticleFeedsResponse(
    val articleFeeds: PrimalEvent,
)
