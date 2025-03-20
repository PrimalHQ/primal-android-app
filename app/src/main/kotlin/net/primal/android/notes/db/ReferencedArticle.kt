package net.primal.android.notes.db

import java.time.Instant
import kotlinx.serialization.Serializable
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.profile.domain.PrimalLegendProfile
import net.primal.domain.CdnImage

@Serializable
data class ReferencedArticle(
    val naddr: String,
    val aTag: String,
    val eventId: String,
    val articleId: String,
    val articleTitle: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarCdnImage: CdnImage?,
    val authorLegendProfile: PrimalLegendProfile?,
    val createdAt: Long,
    val raw: String,
    val articleImageCdnImage: CdnImage? = null,
    val articleReadingTimeInMinutes: Int? = null,
)

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
