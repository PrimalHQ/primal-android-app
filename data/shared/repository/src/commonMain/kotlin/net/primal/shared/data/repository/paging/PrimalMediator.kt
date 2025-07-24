package net.primal.shared.data.repository.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.aakira.napier.Napier
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.fold
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.shared.data.repository.paging.model.PrimalRemoteKey

@OptIn(ExperimentalPagingApi::class)
class PrimalMediator<Key : Any, Value : Any, Request : Any>(
    private val dispatcherProvider: DispatcherProvider,
    private val clearKeysAndConnections: suspend CoroutineScope.() -> Unit,
    private val getLatestRemoteKey: suspend CoroutineScope.() -> PrimalRemoteKey?,
    private val findLastItemRemoteKey: suspend CoroutineScope.(state: PagingState<Key, Value>) -> PrimalRemoteKey?,
    private val buildRequest: suspend (until: Long?, config: PagingConfig, loadType: LoadType) -> Result<Request>,
    private val fetchAndPersistToDatabase: suspend (request: Request) -> Result<Unit>,
) : RemoteMediator<Key, Value>() {

    private val lastRequests: MutableMap<LoadType, Pair<Request, Long>> = mutableMapOf()

    override suspend fun initialize(): InitializeAction {
        Napier.d { "Initializing PrimalMediator" }
        val latestRemoteKeyCachedAt = withContext(dispatcherProvider.io()) {
            getLatestRemoteKey()
        }

        return latestRemoteKeyCachedAt?.let {
            if (it.cachedAt.isTimestampOlderThan(duration = INITIALIZE_CACHE_EXPIRY)) {
                withContext(dispatcherProvider.io()) {
                    clearKeysAndConnections()
                }
                Napier.d { "Launching initial refresh" }
                InitializeAction.LAUNCH_INITIAL_REFRESH
            } else {
                Napier.d { "Skipping initial refresh" }
                InitializeAction.SKIP_INITIAL_REFRESH
            }
        } ?: run {
            withContext(dispatcherProvider.io()) {
                clearKeysAndConnections()
            }
            Napier.d { "Launching initial refresh" }
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Key, Value>,
    ): MediatorResult {
        val nextUntil = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> {
                Napier.d { "PREPEND end of pagination exit." }
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> findLastRemoteKey(state = state)?.sinceId
                ?: run {
                    Napier.d { "APPEND no remote key found exit." }
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
        }

        val request = buildRequest(nextUntil, state.config, loadType).getOrNull()
            ?: return MediatorResult.Error(RuntimeException("Couldn't build request."))

        Napier.d { "LoadType $loadType and request: $request" }
        lastRequests[loadType]?.let { (lastRequest, lastRequestAt) ->
            if (request == lastRequest && !lastRequestAt.isRequestCacheExpired() && loadType != LoadType.REFRESH) {
                Napier.d("RepeatingRequestBody exit.")
                MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return fetchAndPersistToDatabase(request)
            .onSuccess {
                Napier.d { "Fetch and persist successful for $loadType" }
                lastRequests[loadType] = request to Clock.System.now().epochSeconds
            }.onFailure {
                Napier.d(throwable = it) { "Error occurred while fetching and persisting to database. Exiting." }
            }.fold(
                onSuccess = { MediatorResult.Success(endOfPaginationReached = false) },
                onFailure = { MediatorResult.Error(it) },
            )
    }

    private suspend fun findLastRemoteKey(state: PagingState<Key, Value>): PrimalRemoteKey? =
        withContext(dispatcherProvider.io()) {
            findLastItemRemoteKey(state) ?: getLatestRemoteKey()
        }


    private fun Long.isTimestampOlderThan(duration: Long) = (Clock.System.now().epochSeconds - this) > duration

    private fun Long.isRequestCacheExpired() = isTimestampOlderThan(duration = LAST_REQUEST_EXPIRY)

    companion object {
        private val LAST_REQUEST_EXPIRY = 10.seconds.inWholeSeconds
        private val INITIALIZE_CACHE_EXPIRY = 3.minutes.inWholeSeconds
    }
}
