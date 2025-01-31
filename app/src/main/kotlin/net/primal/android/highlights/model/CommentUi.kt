package net.primal.android.highlights.model

import java.time.Instant
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.notes.db.PostWithAuthorData
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization

data class CommentUi(
    val commentId: String,
    val authorId: String,
    val authorDisplayName: String?,
    val authorInternetIdentifier: String?,
    val authorLegendaryCustomization: LegendaryCustomization?,
    val authorCdnImage: CdnImage?,
    val content: String,
    val createdAt: Instant,
)

fun PostWithAuthorData.toCommentUi() =
    CommentUi(
        commentId = this.post.postId,
        authorId = this.author.ownerId,
        authorDisplayName = this.author.displayName,
        authorInternetIdentifier = this.author.internetIdentifier,
        authorLegendaryCustomization = this.author.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
        authorCdnImage = this.author.avatarCdnImage,
        content = this.post.content,
        createdAt = Instant.ofEpochSecond(this.post.createdAt),
    )
