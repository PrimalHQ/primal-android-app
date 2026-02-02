package net.primal.core.networking.nwc.wallet.model

import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.NostrEvent

data class WalletNwcRequestException(
    val nostrEvent: NostrEvent,
    val connection: NwcConnection,
    override val cause: Throwable?,
) : Exception(cause)
