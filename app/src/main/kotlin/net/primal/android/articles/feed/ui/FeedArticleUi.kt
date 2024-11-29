package net.primal.android.articles.feed.ui

import java.time.Instant
import net.primal.android.articles.db.Article
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nip19TLV.toNaddrString
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.premium.legend.asLegendaryCustomization
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
    val authorLegendaryCustomization: LegendaryCustomization? = null,
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
        authorLegendaryCustomization = this.author?.primalLegendProfile?.asLegendaryCustomization(),
    )
}

private const val WordsPerMinute = 200

fun Int?.wordsCountToReadingTime() = ((this ?: 1) / WordsPerMinute) + 1

fun FeedArticleUi.generateNaddrString(): String =
    Naddr(
        identifier = this.articleId,
        userId = this.authorId,
        kind = NostrEventKind.LongFormContent.value,
    ).toNaddrString()
