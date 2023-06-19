package net.primal.android.feed.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

data class ThreadResponse(
    val metadata: List<NostrEvent>,
    val posts: List<NostrEvent>,
    val referencedPosts: List<PrimalEvent>,
    val primalEventStats: List<PrimalEvent>,
    val primalEventUserStats: List<PrimalEvent>,
    val primalEventResources: List<PrimalEvent>,
)