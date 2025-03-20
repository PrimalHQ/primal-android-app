package net.primal.data.repository.mapper

import net.primal.data.local.dao.events.EventZap as EventZapPO
import net.primal.data.utils.authorNameUiFriendly
import net.primal.data.utils.usernameUiFriendly
import net.primal.domain.EventZap as EventZapDO
import net.primal.domain.common.utils.formatNip05Identifier

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
        // TODO Fix this once currency utils are ported
        // this.amountInBtc.toBigDecimal().toSats(),
        amountInSats = 8888.toULong(),
        zapperLegendProfile = this.zapSenderPrimalLegendProfile,
    )
}
