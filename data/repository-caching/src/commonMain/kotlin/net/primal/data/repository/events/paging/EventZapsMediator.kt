package net.primal.data.repository.events.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.events.EventStatsApi
import net.primal.data.remote.api.events.model.EventZapsRequestBody
import net.primal.data.repository.events.processors.persistToDatabaseAsTransaction
import net.primal.domain.common.exception.NetworkException

@ExperimentalPagingApi
class EventZapsMediator(
    private val eventId: String,
    private val userId: String,
    private val dispatcherProvider: DispatcherProvider,
    private val eventStatsApi: EventStatsApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, EventZap>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, EventZap>): MediatorResult =
        withContext(dispatcherProvider.io()) {
            try {
                val response = eventStatsApi.getEventZaps(
                    EventZapsRequestBody(
                        eventId = eventId,
                        userId = userId,
                        limit = 100,
                    ),
                )
                response.persistToDatabaseAsTransaction(database)

                MediatorResult.Success(endOfPaginationReached = true)
            } catch (error: NetworkException) {
                Napier.w(throwable = error) { error.message ?: "" }
                MediatorResult.Error(throwable = error)
            }
        }
}
