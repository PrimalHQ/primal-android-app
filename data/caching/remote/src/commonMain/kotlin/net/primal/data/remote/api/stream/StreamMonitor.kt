package net.primal.data.remote.api.stream

import kotlinx.coroutines.CoroutineScope
import net.primal.domain.nostr.NostrEvent

interface StreamMonitor {

    fun start(
        scope: CoroutineScope,
        creatorPubkey: String,
        dTag: String,
        userPubkey: String,
        onZapEvent: (NostrEvent) -> Unit,
    )

    fun stop(scope: CoroutineScope)
}
