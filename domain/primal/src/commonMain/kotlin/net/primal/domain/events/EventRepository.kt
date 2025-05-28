package net.primal.domain.events

import androidx.paging.PagingData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.common.exception.NetworkException

interface EventRepository {
    fun pagedEventZaps(
        userId: String,
        eventId: String,
        articleATag: String?,
    ): Flow<PagingData<EventZap>>
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
}
