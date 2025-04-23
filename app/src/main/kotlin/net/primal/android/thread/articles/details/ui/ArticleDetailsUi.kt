package net.primal.android.thread.articles.details.ui

import java.time.Instant
import net.primal.android.articles.highlights.HighlightUi
import net.primal.android.articles.highlights.asHighlightUi
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.domain.links.CdnImage
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.reads.Article

data class ArticleDetailsUi(
    val aTag: String,
    val eventId: String,
    val articleId: String,
    val authorId: String,
    val title: String,
    val content: String,
    val summary: String?,
    val publishedAt: Instant,
    val authorDisplayName: String,
    val eventRawNostrEvent: String,
    val authorInternetIdentifier: String? = null,
    val authorAvatarCdnImage: CdnImage? = null,
    val authorBlossoms: List<String> = emptyList(),
    val coverImageCdnImage: CdnImage? = null,
    val readingTimeInMinutes: Int? = null,
    val hashtags: List<String> = emptyList(),
    val isBookmarked: Boolean = false,
    val eventStatsUi: EventStatsUi = EventStatsUi(),
    val authorLegendaryCustomization: LegendaryCustomization? = null,
    val highlights: List<HighlightUi> = emptyList(),
)

fun Article.mapAsArticleDetailsUi(): ArticleDetailsUi {
    return ArticleDetailsUi(
        aTag = this.aTag,
        eventId = this.eventId,
        articleId = this.articleId,
        authorId = this.authorId,
        title = this.title,
        content = this.content,
        summary = this.summary,
        publishedAt = Instant.ofEpochSecond(this.publishedAt),
        authorDisplayName = this.author?.authorNameUiFriendly() ?: this.authorId.asEllipsizedNpub(),
        eventRawNostrEvent = this.articleRawJson,
        authorInternetIdentifier = this.author?.internetIdentifier,
        authorAvatarCdnImage = this.author?.avatarCdnImage,
        authorBlossoms = this.author?.blossoms ?: emptyList(),
        coverImageCdnImage = this.imageCdnImage ?: this.author?.avatarCdnImage,
        readingTimeInMinutes = ((this.wordsCount ?: 1) / 200) + 1,
        hashtags = this.hashtags,
        isBookmarked = this.bookmark != null,
        eventStatsUi = EventStatsUi.from(eventStats = this.eventStats, userStats = this.userEventStats),
        authorLegendaryCustomization = this.author?.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
        highlights = this.highlights.map { it.asHighlightUi() },
    )
}
