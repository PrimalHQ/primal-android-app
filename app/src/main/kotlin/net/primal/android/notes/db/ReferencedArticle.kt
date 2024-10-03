package net.primal.android.notes.db

import kotlinx.serialization.Serializable
import net.primal.android.attachments.domain.CdnImage

@Serializable
data class ReferencedArticle(
    val naddr: String,
    val eventId: String,
    val articleId: String,
    val articleTitle: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarCdnImage: CdnImage?,
    val createdAt: Long,
    val raw: String,
    val articleImageCdnImage: CdnImage? = null,
    val articleReadingTimeInMinutes: Int? = null,
)
