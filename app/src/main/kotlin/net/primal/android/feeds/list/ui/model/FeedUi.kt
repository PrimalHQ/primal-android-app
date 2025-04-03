package net.primal.android.feeds.list.ui.model

import net.primal.domain.FEED_KIND_PRIMAL
import net.primal.domain.FeedSpecKind
import net.primal.domain.model.PrimalFeed

data class FeedUi(
    val ownerId: String,
    val spec: String,
    val specKind: FeedSpecKind,
    val feedKind: String,
    val title: String,
    val description: String,
    val enabled: Boolean = true,
    val deletable: Boolean = true,
)

fun PrimalFeed.asFeedUi() =
    FeedUi(
        ownerId = this.ownerId,
        spec = this.spec,
        specKind = this.specKind,
        title = this.title,
        description = this.description,
        enabled = this.enabled,
        feedKind = this.feedKind,
        deletable = this.feedKind != FEED_KIND_PRIMAL,
    )

fun FeedUi.asPrimalFeed() =
    PrimalFeed(
        ownerId = this.ownerId,
        spec = this.spec,
        specKind = this.specKind,
        title = this.title,
        description = this.description,
        enabled = this.enabled,
        feedKind = this.feedKind,
    )
