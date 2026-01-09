package net.primal.domain.global

import net.primal.core.utils.Result
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent

interface CachingImportRepository {
    suspend fun cacheNostrEvents(vararg events: NostrEvent)
    suspend fun cacheNostrEvents(events: List<NostrEvent>)
    suspend fun cachePrimalEvents(vararg events: PrimalEvent)
    suspend fun cachePrimalEvents(events: List<PrimalEvent>)
    suspend fun cacheEvents(nostrEvents: List<NostrEvent>, primalEvents: List<PrimalEvent>)

    suspend fun importEvents(events: List<NostrEvent>): Boolean
    suspend fun broadcastEvents(events: List<NostrEvent>, relays: List<String>): Result<List<BroadcastEventResponse>>
}
