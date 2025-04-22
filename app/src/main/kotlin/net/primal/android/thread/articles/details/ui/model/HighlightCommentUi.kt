package net.primal.android.thread.articles.details.ui.model

import java.time.Instant
import kotlinx.datetime.toJavaInstant
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.domain.links.CdnImage
import net.primal.domain.posts.FeedPost

data class HighlightCommentUi(
    val commentId: String,
    val authorId: String?,
    val authorDisplayName: String?,
    val authorInternetIdentifier: String?,
    val authorLegendaryCustomization: LegendaryCustomization?,
    val authorCdnImage: CdnImage?,
    val content: String,
    val createdAt: Instant,
)

fun FeedPost.toHighlightCommentUi() =
    HighlightCommentUi(
        commentId = this.eventId,
        authorId = this.author.authorId,
        authorDisplayName = this.author.displayName,
        authorInternetIdentifier = this.author.internetIdentifier,
        authorLegendaryCustomization = this.author.legendProfile?.asLegendaryCustomization(),
        authorCdnImage = this.author.avatarCdnImage,
        content = this.content,
        createdAt = this.timestamp.toJavaInstant(),
    )
