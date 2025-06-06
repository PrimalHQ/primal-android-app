package net.primal.data.repository.mappers.local

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.data.local.dao.events.EventZap as EventZapPO
import net.primal.data.repository.mappers.authorNameUiFriendly
import net.primal.data.repository.mappers.usernameUiFriendly
import net.primal.domain.events.EventZap as EventZapDO
import net.primal.domain.nostr.utils.formatNip05Identifier

internal fun EventZapPO.asEventZapDO(): EventZapDO {
    return EventZapDO(
        id = "${this.zapSenderId};${this.eventId};${this.zapRequestAt}",
        zapperAvatarCdnImage = this.zapSenderAvatarCdnImage,
        zapperId = this.zapSenderId,
        zapperName = this.authorNameUiFriendly(),
        zapperHandle = this.usernameUiFriendly(),
        zapperInternetIdentifier = this.zapSenderInternetIdentifier?.formatNip05Identifier(),
        zappedAt = this.zapRequestAt,
        message = this.message,
        amountInSats = this.amountInBtc.toBigDecimal().toSats(),
        zapperLegendProfile = this.zapSenderPrimalLegendProfile,
    )
}
