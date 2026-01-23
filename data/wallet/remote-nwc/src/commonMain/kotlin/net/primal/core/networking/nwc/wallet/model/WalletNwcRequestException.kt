package net.primal.core.networking.nwc.wallet.model

import net.primal.domain.nostr.NostrEvent

data class WalletNwcRequestException(
    val nostrEvent: NostrEvent,
    override val cause: Throwable?,
) : Exception(cause)
