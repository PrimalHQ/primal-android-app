package net.primal.android.feeds.ui.model

import net.primal.android.articles.db.ArticleFeed

data class FeedUi(
    val directive: String,
    val name: String,
    val description: String,
    val enabled: Boolean = true,
    val deletable: Boolean = true,
    val kind: String? = null,
)

fun ArticleFeed.asFeedUi() =
    FeedUi(
        directive = this.spec,
        name = this.name,
        description = this.description,
        enabled = this.enabled,
        kind = this.kind,
        deletable = this.kind != "primal",
    )

fun FeedUi.asArticleFeedDb() =
    ArticleFeed(
        spec = this.directive,
        name = this.name,
        description = this.description,
        enabled = this.enabled,
        kind = this.kind,
    )
