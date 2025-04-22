package net.primal.android.nostr.mappers

import java.time.Instant
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.domain.links.ReferencedArticle

fun ReferencedArticle.asFeedArticleUi() =
    FeedArticleUi(
        aTag = this.aTag,
        eventId = this.eventId,
        articleId = this.articleId,
        title = this.articleTitle,
        content = "",
        publishedAt = Instant.ofEpochSecond(this.createdAt),
        authorId = this.authorId,
        authorName = this.authorName,
        rawNostrEventJson = this.raw,
        isBookmarked = false,
        stats = EventStatsUi(),
        authorAvatarCdnImage = this.authorAvatarCdnImage,
        authorLegendaryCustomization = this.authorLegendProfile?.asLegendaryCustomization(),
        imageCdnImage = this.articleImageCdnImage,
        readingTimeInMinutes = this.articleReadingTimeInMinutes,
    )
