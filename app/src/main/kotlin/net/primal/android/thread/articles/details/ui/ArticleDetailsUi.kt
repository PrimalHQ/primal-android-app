package net.primal.android.thread.articles.details.ui

import java.time.Instant
import net.primal.android.articles.db.Article
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.notes.feed.model.EventStatsUi

data class ArticleDetailsUi(
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
    val eventStatsUi: EventStatsUi = EventStatsUi(),
)

fun Article.mapAsArticleDetailsUi(): ArticleDetailsUi {
    return ArticleDetailsUi(
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
        eventStatsUi = EventStatsUi.from(eventStats = this.eventStats, userStats = this.userEventStats),
    )
}
