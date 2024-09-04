package net.primal.android.feeds.ui.model

import net.primal.android.feeds.db.Feed
import net.primal.android.feeds.domain.FEED_KIND_PRIMAL
import net.primal.android.feeds.domain.FeedSpecKind

data class FeedUi(
    val spec: String,
    val specKind: FeedSpecKind,
    val name: String,
    val description: String,
    val enabled: Boolean = true,
    val deletable: Boolean = true,
    val feedKind: String? = null,
)

fun Feed.asFeedUi() =
    FeedUi(
        spec = this.spec,
        specKind = this.specKind,
        name = this.name,
        description = this.description,
        enabled = this.enabled,
        feedKind = this.feedKind,
        deletable = this.feedKind != FEED_KIND_PRIMAL,
    )

fun FeedUi.asFeedPO() =
    Feed(
        spec = this.spec,
        specKind = this.specKind,
        name = this.name,
        description = this.description,
        enabled = this.enabled,
        feedKind = this.feedKind,
    )
