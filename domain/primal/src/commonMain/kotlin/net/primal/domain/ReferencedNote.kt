package net.primal.domain

import kotlinx.serialization.Serializable

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
    val attachments: List<EventLink>,
    val nostrUris: List<EventUriNostrReference>,
    val raw: String,
)
