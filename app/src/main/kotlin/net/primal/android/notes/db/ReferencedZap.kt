package net.primal.android.notes.db

import kotlinx.serialization.Serializable
import net.primal.android.events.db.EventUriNostr
import net.primal.domain.CdnImage
import net.primal.domain.PrimalLegendProfile

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
