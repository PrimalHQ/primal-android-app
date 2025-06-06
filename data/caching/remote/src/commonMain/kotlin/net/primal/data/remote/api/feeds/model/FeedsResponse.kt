package net.primal.data.remote.api.feeds.model

import kotlinx.serialization.Serializable
import net.primal.domain.common.PrimalEvent

@Serializable
data class FeedsResponse(
    val articleFeeds: PrimalEvent,
)
