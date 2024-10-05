package net.primal.android.feeds.repository

import net.primal.android.feeds.db.Feed
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.nostr.model.primal.content.ContentArticleFeedData

fun ContentArticleFeedData.asFeedPO(specKind: FeedSpecKind): Feed {
    return Feed(
        spec = this.spec,
        specKind = specKind,
        title = this.name,
        description = this.description,
        enabled = this.enabled,
        feedKind = this.feedKind,
    )
}
