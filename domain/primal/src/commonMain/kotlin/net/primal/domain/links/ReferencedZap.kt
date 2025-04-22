package net.primal.domain.links

import kotlinx.serialization.Serializable
import net.primal.domain.premium.PrimalLegendProfile

@Serializable
data class ReferencedZap(
    val senderId: String,
    val senderAvatarCdnImage: CdnImage? = null,
    val senderPrimalLegendProfile: PrimalLegendProfile? = null,
    val receiverId: String,
    val receiverDisplayName: String?,
    val receiverAvatarCdnImage: CdnImage? = null,
    val receiverPrimalLegendProfile: PrimalLegendProfile? = null,
    val zappedEventId: String?,
    val zappedEventContent: String?,
    val zappedEventNostrUris: List<EventUriNostrReference>,
    val zappedEventHashtags: List<String>,
    val amountInSats: Double,
    val message: String?,
    val createdAt: Long,
)
