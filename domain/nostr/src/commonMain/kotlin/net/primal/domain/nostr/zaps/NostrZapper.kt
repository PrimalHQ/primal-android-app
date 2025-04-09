package net.primal.domain.nostr.zaps

import kotlin.coroutines.cancellation.CancellationException

interface NostrZapper {
    @Throws(
        ZapException::class,
        CancellationException::class,
    )
    suspend fun zap(data: ZapRequestData)
}
