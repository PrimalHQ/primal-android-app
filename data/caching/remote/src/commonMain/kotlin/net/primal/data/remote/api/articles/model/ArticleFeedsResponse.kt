package net.primal.data.remote.api.articles.model

import kotlinx.serialization.Serializable
import net.primal.domain.common.PrimalEvent

@Serializable
data class ArticleFeedsResponse(
    val articleFeeds: PrimalEvent,
)
