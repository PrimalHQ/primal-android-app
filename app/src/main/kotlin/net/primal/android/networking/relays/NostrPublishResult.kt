package net.primal.android.networking.relays

import net.primal.core.networking.sockets.NostrIncomingMessage

data class NostrPublishResult(
    val result: NostrIncomingMessage? = null,
    val error: Throwable? = null,
)
