package net.primal.android.thread.articles.details.ui

import java.time.Instant
import net.primal.android.articles.db.Article
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.premium.legend.LegendaryStyle

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
    val coverImageCdnImage: CdnImage? = null,
    val readingTimeInMinutes: Int? = null,
    val hashtags: List<String> = emptyList(),
    val isBookmarked: Boolean = false,
    val eventStatsUi: EventStatsUi = EventStatsUi(),
    val authorLegendAvatarGlow: Boolean = false,
    val authorLegendCustomBadge: Boolean = false,
    val authorLegendaryStyle: LegendaryStyle? = null,
)

fun Article.mapAsArticleDetailsUi(): ArticleDetailsUi {
    return ArticleDetailsUi(
        aTag = this.data.aTag,
        eventId = this.data.eventId,
        articleId = this.data.articleId,
        authorId = this.data.authorId,
        title = this.data.title,
        content = this.data.content,
        summary = this.data.summary,
        publishedAt = Instant.ofEpochSecond(this.data.publishedAt),
        authorDisplayName = this.author?.authorNameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
        eventRawNostrEvent = this.data.raw,
        authorInternetIdentifier = this.author?.internetIdentifier,
        authorAvatarCdnImage = this.author?.avatarCdnImage,
        coverImageCdnImage = this.data.imageCdnImage ?: this.author?.avatarCdnImage,
        readingTimeInMinutes = ((this.data.wordsCount ?: 1) / 200) + 1,
        hashtags = this.data.hashtags,
        isBookmarked = this.bookmark != null,
        eventStatsUi = EventStatsUi.from(eventStats = this.eventStats, userStats = this.userEventStats),
        authorLegendAvatarGlow = this.author?.primalLegendProfile?.avatarGlow == true,
        authorLegendCustomBadge = this.author?.primalLegendProfile?.customBadge == true,
        authorLegendaryStyle = LegendaryStyle.valueById(this.author?.primalLegendProfile?.styleId),
    )
}
