package net.primal.data.local.dao.events

import kotlinx.serialization.Serializable
import net.primal.domain.CdnImage
import net.primal.domain.PrimalLegendProfile

@Serializable
data class ReferencedNote(
    val postId: String,
    val createdAt: Long,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarCdnImage: CdnImage?,
    val authorInternetIdentifier: String?,
    val authorLightningAddress: String?,
    val authorLegendProfile: PrimalLegendProfile?,
    val attachments: List<EventUri>,
    val nostrUris: List<EventUriNostr>,
    val raw: String,
)
