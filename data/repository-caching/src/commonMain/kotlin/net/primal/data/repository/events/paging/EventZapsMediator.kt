package net.primal.data.repository.events.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.events.EventStatsApi
import net.primal.data.remote.api.events.model.EventZapsRequestBody
import net.primal.data.repository.events.processors.persistToDatabaseAsTransaction

@ExperimentalPagingApi
class EventZapsMediator(
    private val eventId: String,
    private val userId: String,
    private val dispatcherProvider: DispatcherProvider,
    private val eventStatsApi: EventStatsApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, EventZap>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, EventZap>): MediatorResult {
        withContext(dispatcherProvider.io()) {
            val response = eventStatsApi.getEventZaps(
                EventZapsRequestBody(
                    eventId = eventId,
                    userId = userId,
                    limit = 100,
                ),
            )
            response.persistToDatabaseAsTransaction(database)
        }
        return MediatorResult.Success(endOfPaginationReached = true)
    }
}
