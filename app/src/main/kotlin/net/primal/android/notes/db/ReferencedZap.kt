package net.primal.android.notes.db

import kotlinx.serialization.Serializable
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.profile.domain.PrimalLegendProfile

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
    val amountInSats: Double,
    val message: String?,
)
