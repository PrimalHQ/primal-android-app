package net.primal.domain.links

import kotlinx.serialization.Serializable
import net.primal.domain.membership.PrimalLegendProfile
import net.primal.domain.polls.PollInfo

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
    val pollInfo: PollInfo? = null,
)
