package net.primal.data.repository.mappers.remote

import net.primal.data.local.dao.feeds.Feed as FeedPO
import net.primal.data.remote.model.ContentPrimalFeedData
import net.primal.domain.feeds.FeedSpecKind

fun ContentPrimalFeedData.asFeedPO(ownerId: String, specKind: FeedSpecKind): FeedPO {
    return FeedPO(
        ownerId = ownerId,
        spec = this.spec,
        specKind = specKind,
        title = this.name,
        description = this.description,
        enabled = this.enabled,
        feedKind = this.feedKind,
    )
}
