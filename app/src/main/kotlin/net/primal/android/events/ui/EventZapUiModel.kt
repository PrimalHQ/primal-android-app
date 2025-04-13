package net.primal.android.events.ui

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.domain.events.EventZap
import net.primal.domain.links.CdnImage

data class EventZapUiModel(
    val id: String,
    val zapperId: String,
    val zapperName: String,
    val zapperHandle: String,
    val zappedAt: Long,
    val message: String?,
    val amountInSats: ULong,
    val zapperInternetIdentifier: String? = null,
    val zapperAvatarCdnImage: CdnImage? = null,
    val zapperLegendaryCustomization: LegendaryCustomization? = null,
) {
    companion object {
        val DefaultComparator = compareByDescending<EventZapUiModel> { it.amountInSats }.thenBy { it.zappedAt }
    }
}

fun EventZap.asEventZapUiModel() =
    EventZapUiModel(
        id = this.id,
        zapperAvatarCdnImage = this.zapperAvatarCdnImage,
        zapperId = this.zapperId,
        zapperName = this.zapperName,
        zapperHandle = this.zapperHandle,
        zapperInternetIdentifier = this.zapperInternetIdentifier,
        zappedAt = this.zappedAt,
        message = this.message,
        amountInSats = this.amountInSats,
        zapperLegendaryCustomization = this.zapperLegendProfile?.asLegendaryCustomization(),
    )
