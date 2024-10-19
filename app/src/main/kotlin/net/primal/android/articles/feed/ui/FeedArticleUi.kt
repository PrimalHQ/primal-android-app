package net.primal.android.articles.feed.ui

import java.time.Instant
import net.primal.android.articles.db.Article
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.stats.ui.EventZapUiModel
import net.primal.android.stats.ui.asEventZapUiModel

data class FeedArticleUi(
    val aTag: String,
    val eventId: String,
    val articleId: String,
    val title: String,
    val content: String,
    val publishedAt: Instant,
    val authorId: String,
    val authorName: String,
    val rawNostrEventJson: String?,
    val isBookmarked: Boolean,
    val stats: EventStatsUi,
    val authorAvatarCdnImage: CdnImage? = null,
    val imageCdnImage: CdnImage? = null,
    val readingTimeInMinutes: Int? = null,
    val eventZaps: List<EventZapUiModel> = emptyList(),
)

fun Article.mapAsFeedArticleUi(): FeedArticleUi {
    return FeedArticleUi(
        aTag = this.data.aTag,
        eventId = this.data.eventId,
        articleId = this.data.articleId,
        publishedAt = Instant.ofEpochSecond(this.data.publishedAt),
        authorId = this.data.authorId,
        title = this.data.title,
        content = this.data.content,
        rawNostrEventJson = this.data.raw,
        imageCdnImage = this.data.imageCdnImage ?: this.author?.avatarCdnImage,
        authorName = this.author?.authorNameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
        authorAvatarCdnImage = this.author?.avatarCdnImage,
        isBookmarked = this.bookmark != null,
        stats = EventStatsUi.from(eventStats = this.eventStats, userStats = null),
        readingTimeInMinutes = this.data.wordsCount.wordsCountToReadingTime(),
        eventZaps = this.eventZaps.map { it.asEventZapUiModel() },
    )
}

fun Int?.wordsCountToReadingTime() = ((this ?: 1) / 200) + 1
