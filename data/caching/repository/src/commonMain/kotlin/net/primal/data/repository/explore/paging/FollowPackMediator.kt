package net.primal.data.repository.explore.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withContext
import net.primal.core.caching.MediaCacher
import net.primal.core.networking.utils.orderByPagingIfNotNull
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.explore.FollowPack as FollowPackPO
import net.primal.data.local.dao.explore.FollowPackListCrossRef
import net.primal.data.local.dao.explore.FollowPackRemoteKey
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.explore.ExploreApi
import net.primal.data.remote.api.explore.model.FollowListsRequestBody
import net.primal.data.remote.api.explore.model.FollowListsResponse
import net.primal.data.repository.explore.mapAsFollowPackData
import net.primal.data.repository.explore.processAndPersistFollowLists
import net.primal.data.repository.utils.cacheAvatarUrls
import net.primal.domain.common.ContentPrimalPaging
import net.primal.domain.common.exception.NetworkException
import net.primal.shared.data.local.db.withTransaction

@ExperimentalPagingApi
internal class FollowPackMediator(
    private val exploreApi: ExploreApi,
    private val database: PrimalDatabase,
    private val dispatcherProvider: DispatcherProvider,
    private val mediaCacher: MediaCacher? = null,
) : RemoteMediator<Int, FollowPackPO>() {

    private val lastRequests: MutableMap<LoadType, Pair<FollowListsRequestBody, Long>> =
        mutableMapOf()

    override suspend fun initialize(): InitializeAction {
        val latestRemoteKey = withContext(dispatcherProvider.io()) {
            database.followPackRemoteKeys().findLatest()
        }

        return latestRemoteKey?.let {
            if (it.cachedAt.isTimestampOlderThan(duration = INITIALIZE_CACHE_EXPIRY)) {
                clearKeysAndConnections()
                InitializeAction.LAUNCH_INITIAL_REFRESH
            } else {
                InitializeAction.SKIP_INITIAL_REFRESH
            }
        } ?: run {
            clearKeysAndConnections()
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, FollowPackPO>): MediatorResult {
        val nextUntil = when (loadType) {
            LoadType.APPEND -> findLastRemoteKey(state = state)?.sinceId
                ?: run {
                    Napier.d("APPEND no remote key found exit.")
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.REFRESH -> null
        }

        return try {
            val response = fetchFollowPacks(
                pageSize = state.config.pageSize,
                nextUntil = nextUntil,
                loadType = loadType,
            )
            mediaCacher?.cacheAvatarUrls(metadata = response.metadata, cdnResources = response.cdnResources)

            processAndPersistToDatabase(
                response = response,
                clearFeed = loadType == LoadType.REFRESH,
            )

            MediatorResult.Success(endOfPaginationReached = false)
        } catch (error: NetworkException) {
            MediatorResult.Error(error)
        } catch (_: RepeatingRequestBodyException) {
            Napier.d("RepeatingRequestBody exit.")
            MediatorResult.Success(endOfPaginationReached = true)
        }
    }

    private suspend fun processAndPersistToDatabase(response: FollowListsResponse, clearFeed: Boolean) {
        val connections = response.followListEvents
            .orderByPagingIfNotNull(pagingEvent = response.pagingEvent)
            .mapAsFollowPackData(cdnResourcesMap = emptyMap()).map {
                FollowPackListCrossRef(followPackATag = it.first.aTag)
            }

        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                if (clearFeed) {
                    database.followPackRemoteKeys().deleteAll()
                    database.followPacksConnections().deleteConnections()
                }

                database.followPacksConnections().connect(data = connections)

                response.processAndPersistFollowLists(database = database)
            }
        }

        connections.processRemoteKeys(pagingEvent = response.pagingEvent)
    }

    private suspend fun List<FollowPackListCrossRef>.processRemoteKeys(pagingEvent: ContentPrimalPaging?) {
        val sinceId = pagingEvent?.sinceId
        val untilId = pagingEvent?.untilId
        if (sinceId != null && untilId != null) {
            val remoteKeys = map {
                FollowPackRemoteKey(
                    followPackATag = it.followPackATag,
                    sinceId = sinceId,
                    untilId = untilId,
                    cachedAt = Clock.System.now().epochSeconds,
                )
            }

            database.followPackRemoteKeys().upsertAll(data = remoteKeys)
        }
    }

    private suspend fun fetchFollowPacks(
        pageSize: Int,
        nextUntil: Long?,
        loadType: LoadType,
    ): FollowListsResponse {
        val request = FollowListsRequestBody(
            limit = pageSize,
            until = nextUntil,
        )

        lastRequests[loadType]?.let { (lastRequest, lastRequestAt) ->
            if (request == lastRequest && !lastRequestAt.isRequestCacheExpired() && loadType != LoadType.REFRESH) {
                throw RepeatingRequestBodyException()
            }
        }

        val response = withContext(dispatcherProvider.io()) {
            retryNetworkCall {
                exploreApi.getFollowLists(body = request)
            }
        }

        lastRequests[loadType] = request to Clock.System.now().epochSeconds
        return response
    }

    private suspend fun clearKeysAndConnections() =
        withContext(dispatcherProvider.io()) {
            database.followPackRemoteKeys().deleteAll()
            database.followPacksConnections().deleteConnections()
        }

    private suspend fun findLastRemoteKey(state: PagingState<Int, FollowPackPO>): FollowPackRemoteKey? {
        val lastItemATag = state.lastItemOrNull()?.data?.aTag
            ?: findLastItemOrNull()?.followPackATag

        return withContext(dispatcherProvider.io()) {
            lastItemATag?.let {
                database.followPackRemoteKeys().find(aTag = lastItemATag)
            }
                ?: database.followPackRemoteKeys().findLatest()
        }
    }

    private suspend fun findLastItemOrNull(): FollowPackListCrossRef? =
        withContext(dispatcherProvider.io()) {
            database.followPacksConnections().findLast()
        }

    private fun Long.isTimestampOlderThan(duration: Long) = (Clock.System.now().epochSeconds - this) > duration

    private fun Long.isRequestCacheExpired() = isTimestampOlderThan(duration = LAST_REQUEST_EXPIRY)

    private inner class RepeatingRequestBodyException : RuntimeException()

    companion object {
        private val LAST_REQUEST_EXPIRY = 10.seconds.inWholeSeconds
        private val INITIALIZE_CACHE_EXPIRY = 3.minutes.inWholeSeconds
    }
}
