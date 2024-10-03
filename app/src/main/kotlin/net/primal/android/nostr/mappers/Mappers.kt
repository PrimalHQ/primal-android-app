package net.primal.android.nostr.mappers

import net.primal.android.feeds.db.Feed
import net.primal.android.nostr.model.primal.content.ContentArticleFeedData

fun Feed.asContentArticleFeedData() =
    ContentArticleFeedData(
        name = this.name,
        spec = this.spec,
        feedKind = this.feedKind,
        description = this.description,
        enabled = this.enabled,
    )
