package net.primal.android.articles.feed.ui

import java.time.Instant
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.events.ui.asEventZapUiModel
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.domain.CdnImage
import net.primal.domain.model.Article
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.utils.asEllipsizedNpub

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
        aTag = this.aTag,
        eventId = this.eventId,
        articleId = this.articleId,
        publishedAt = Instant.ofEpochSecond(this.publishedAt),
        authorId = this.authorId,
        title = this.title,
        content = this.content,
        rawNostrEventJson = this.articleRawJson,
        imageCdnImage = this.imageCdnImage ?: this.author?.avatarCdnImage,
        authorName = this.author?.authorNameUiFriendly() ?: this.authorId.asEllipsizedNpub(),
        authorAvatarCdnImage = this.author?.avatarCdnImage,
        isBookmarked = this.bookmark != null,
        stats = EventStatsUi.from(eventStats = this.eventStats, userStats = null),
        readingTimeInMinutes = this.wordsCount.wordsCountToReadingTime(),
        eventZaps = this.eventZaps.map { it.asEventZapUiModel() },
        authorLegendaryCustomization = this.author?.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
    )
}

private const val WordsPerMinute = 200

fun Int?.wordsCountToReadingTime() = ((this ?: 1) / WordsPerMinute) + 1

fun FeedArticleUi.generateNaddr() =
    Naddr(
        identifier = this.articleId,
        userId = this.authorId,
        kind = NostrEventKind.LongFormContent.value,
    )

fun FeedArticleUi.generateNaddrString() = generateNaddr().toString()
