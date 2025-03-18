package net.primal.android.networking.relays

import net.primal.networking.sockets.NostrIncomingMessage

data class NostrPublishResult(
    val result: NostrIncomingMessage? = null,
    val error: Throwable? = null,
)
