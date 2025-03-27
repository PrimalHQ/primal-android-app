package net.primal.android.events.ui

import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.events.db.EventZap
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats
import net.primal.domain.CdnImage

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
        id = "${this.zapSenderId};${this.eventId};${this.zapRequestAt}",
        zapperAvatarCdnImage = this.zapSenderAvatarCdnImage,
        zapperId = this.zapSenderId,
        zapperName = this.authorNameUiFriendly(),
        zapperHandle = this.usernameUiFriendly(),
        zapperInternetIdentifier = this.zapSenderInternetIdentifier?.formatNip05Identifier(),
        zappedAt = this.zapRequestAt,
        message = this.message,
        amountInSats = this.amountInBtc.toBigDecimal().toSats(),
        zapperLegendaryCustomization = this.zapSenderPrimalLegendProfile?.asLegendaryCustomization(),
    )

fun net.primal.domain.EventZap.asEventZapUiModel() =
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
