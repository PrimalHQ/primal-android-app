package net.primal.domain.events

import androidx.paging.PagingData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.NostrEvent

interface EventRepository {
    fun pagedEventZaps(
        userId: String,
        eventId: String,
        articleATag: String?,
    ): Flow<PagingData<EventZap>>

    suspend fun observeZapsByEventId(eventId: String): Flow<List<EventZap>>

    fun observeEventStats(eventIds: List<String>): Flow<List<NostrEventStats>>
    fun observeUserEventStatus(eventIds: List<String>, userId: String): Flow<List<NostrEventUserStats>>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchEventActions(eventId: String, kind: Int): List<NostrEventAction>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchEventZaps(
        userId: String,
        eventId: String,
        limit: Int,
    )

    suspend fun fetchReplaceableEvent(naddr: Naddr): Result<Unit>

    suspend fun fetchReplaceableEvents(naddrs: List<Naddr>): Result<Unit>

    suspend fun getZapReceipts(invoices: List<String>): Result<Map<String, NostrEvent>>

    suspend fun saveZapRequest(invoice: String, zapRequestEvent: NostrEvent)

    suspend fun deleteZapRequest(invoice: String)
}
