package net.primal.android.feeds.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.primal.PrimalEvent

@Serializable
data class FeedsResponse(
    val articleFeeds: PrimalEvent,
)
