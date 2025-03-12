package net.primal.db.events

import kotlinx.serialization.Serializable
import net.primal.db.profiles.PrimalLegendProfile
import net.primal.domain.CdnImage

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
    val zappedEventNostrUris: List<EventUriNostr>,
    val zappedEventHashtags: List<String>,
    val amountInSats: Double,
    val message: String?,
    val createdAt: Long,
)
