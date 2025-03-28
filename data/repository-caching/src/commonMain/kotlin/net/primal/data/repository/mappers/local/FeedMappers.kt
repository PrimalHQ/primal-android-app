package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.feeds.Feed as FeedPO
import net.primal.data.remote.model.ContentPrimalFeedData
import net.primal.domain.model.PrimalFeed

fun FeedPO.asPrimalFeedDO(): PrimalFeed {
    return PrimalFeed(
        ownerId = this.ownerId,
        spec = this.spec,
        specKind = this.specKind,
        feedKind = this.feedKind,
        title = this.title,
        description = this.description,
        enabled = this.enabled,
        position = this.position,
    )
}

fun PrimalFeed.asFeedPO(): FeedPO {
    return FeedPO(
        ownerId = this.ownerId,
        spec = this.spec,
        specKind = this.specKind,
        feedKind = this.feedKind,
        title = this.title,
        description = this.description,
        enabled = this.enabled,
        position = this.position,
    )
}

fun PrimalFeed.asContentPrimalFeedData(): ContentPrimalFeedData =
    ContentPrimalFeedData(
        name = this.title,
        spec = this.spec,
        feedKind = this.feedKind,
        description = this.description,
        enabled = this.enabled,
    )
