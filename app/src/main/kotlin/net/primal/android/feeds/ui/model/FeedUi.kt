package net.primal.android.feeds.ui.model

import net.primal.android.feeds.db.Feed
import net.primal.android.feeds.domain.FEED_KIND_PRIMAL
import net.primal.android.feeds.domain.FeedSpecKind

data class FeedUi(
    val spec: String,
    val specKind: FeedSpecKind,
    val feedKind: String,
    val title: String,
    val description: String,
    val enabled: Boolean = true,
    val deletable: Boolean = true,
)

fun Feed.asFeedUi() =
    FeedUi(
        spec = this.spec,
        specKind = this.specKind,
        title = this.title,
        description = this.description,
        enabled = this.enabled,
        feedKind = this.feedKind,
        deletable = this.feedKind != FEED_KIND_PRIMAL,
    )

fun FeedUi.asFeedPO() =
    Feed(
        spec = this.spec,
        specKind = this.specKind,
        title = this.title,
        description = this.description,
        enabled = this.enabled,
        feedKind = this.feedKind,
    )
