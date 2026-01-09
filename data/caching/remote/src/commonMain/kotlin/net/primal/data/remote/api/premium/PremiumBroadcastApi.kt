package net.primal.data.remote.api.premium

import net.primal.domain.nostr.NostrEvent
import net.primal.domain.premium.BroadcastingStatus

interface PremiumBroadcastApi {
    suspend fun getContentStats(userId: String, signedAppSpecificDataNostrEvent: NostrEvent): Map<Int, Long>

    suspend fun startContentRebroadcast(
        userId: String,
        kinds: List<Int>?,
        signedAppSpecificDataNostrEvent: NostrEvent,
    )

    suspend fun cancelContentRebroadcast(userId: String, signedAppSpecificDataNostrEvent: NostrEvent)

    suspend fun getContentRebroadcastStatus(
        userId: String,
        signedAppSpecificDataNostrEvent: NostrEvent,
    ): BroadcastingStatus
}
