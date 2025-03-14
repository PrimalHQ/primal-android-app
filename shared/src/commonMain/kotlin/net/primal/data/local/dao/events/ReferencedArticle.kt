package net.primal.data.local.dao.events

import kotlinx.serialization.Serializable
import net.primal.domain.CdnImage
import net.primal.domain.PrimalLegendProfile

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
