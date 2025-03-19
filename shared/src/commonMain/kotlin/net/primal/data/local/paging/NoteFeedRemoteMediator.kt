package net.primal.data.local.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.aakira.napier.Napier
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.io.IOException
import net.primal.core.networking.sockets.errors.NostrNoticeException
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.notes.FeedPost
import net.primal.data.local.dao.notes.FeedPostRemoteKey
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.queries.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.data.local.queries.ExploreFeedQueryBuilder
import net.primal.data.local.queries.FeedQueryBuilder
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.model.FeedBySpecRequestBody
import net.primal.data.remote.api.feed.model.FeedResponse
import net.primal.data.remote.processors.FeedProcessor
import net.primal.domain.isNotesBookmarkFeedSpec
import net.primal.domain.isProfileAuthoredNoteRepliesFeedSpec
import net.primal.domain.isProfileAuthoredNotesFeedSpec
import net.primal.domain.supportsNoteReposts
import net.primal.domain.supportsUpwardsNotesPagination

@ExperimentalPagingApi
internal class NoteFeedRemoteMediator(
    private val dispatcherProvider: DispatcherProvider,
    private val feedSpec: String,
    private val userId: String,
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, FeedPost>() {

    private val feedQueryBuilder: FeedQueryBuilder = when {
        feedSpec.supportsNoteReposts() -> ChronologicalFeedWithRepostsQueryBuilder(
            feedSpec = feedSpec,
            userPubkey = userId,
        )

        else -> ExploreFeedQueryBuilder(
            feedSpec = feedSpec,
            userPubkey = userId,
        )
    }

    private val lastRequests: MutableMap<LoadType, Pair<FeedBySpecRequestBody, Long>> = mutableMapOf()

    private val feedProcessor: FeedProcessor = FeedProcessor(feedSpec = feedSpec, database = database)

    private suspend fun String.isLastCacheTimestampOlderThan(duration: Duration): Boolean {
        val lastCachedAt = withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys()
                .lastCachedAt(ownerId = userId, directive = this@isLastCacheTimestampOlderThan)
        } ?: return true

        return lastCachedAt < Clock.System.now().minus(duration).epochSeconds
    }

    private suspend fun shouldResetLocalCache() =
        when {
            feedSpec.isNotesBookmarkFeedSpec() -> true
            feedSpec.isProfileAuthoredNotesFeedSpec() -> true
            feedSpec.isProfileAuthoredNoteRepliesFeedSpec() -> true
            feedSpec.supportsUpwardsNotesPagination() -> feedSpec.isLastCacheTimestampOlderThan(duration = 24.hours)
            else -> feedSpec.isLastCacheTimestampOlderThan(duration = 3.minutes)
        }

    override suspend fun initialize(): InitializeAction {
        return when {
            shouldResetLocalCache() -> {
                clearFeedSpec(feedSpec = feedSpec)
                InitializeAction.LAUNCH_INITIAL_REFRESH
            }

            else -> {
                InitializeAction.SKIP_INITIAL_REFRESH
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override suspend fun load(loadType: LoadType, state: PagingState<Int, FeedPost>): MediatorResult {
        Napier.i("feed_spec $feedSpec load called ($loadType)")
        if (loadType == LoadType.PREPEND) {
            Napier.w("feed_spec $feedSpec load exit 9")
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val remoteKey = when (loadType) {
//                LoadType.PREPEND -> findFirstFeedPostRemoteKey(state = state)
                LoadType.APPEND -> findLastFeedPostRemoteKey(state = state)
                else -> null
            }

            if (remoteKey == null && loadType != LoadType.REFRESH) {
                throw RemoteKeyNotFoundException()
            }

            withContext(dispatcherProvider.io()) {
                syncFeed(
                    loadType = loadType,
                    pagingState = state,
                    remoteKey = remoteKey,
                )
            }

            Napier.w("feed_spec $feedSpec load exit 6")
            MediatorResult.Success(endOfPaginationReached = false)
        } catch (error: IOException) {
            Napier.w("feed_spec $feedSpec load exit 7", error)
            MediatorResult.Error(error)
        } catch (error: NostrNoticeException) {
            Napier.w("feed_spec $feedSpec load exit 8", error)
            MediatorResult.Error(error)
        } catch (error: NoSuchFeedPostException) {
            Napier.w("feed_spec $feedSpec load exit 2", error)
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (error: RemoteKeyNotFoundException) {
            Napier.w("feed_spec $feedSpec load exit 3", error)
            MediatorResult.Error(error)
        } catch (error: WssException) {
            Napier.w("feed_spec $feedSpec load exit 5", error)
            MediatorResult.Error(error)
        } catch (error: RepeatingRequestBodyException) {
            Napier.w("feed_spec $feedSpec load exit 4", error)
            MediatorResult.Success(endOfPaginationReached = true)
        }
    }

    private suspend fun clearFeedSpec(feedSpec: String) =
        withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys().deleteByDirective(ownerId = userId, directive = feedSpec)
            database.feedsConnections().deleteConnectionsByDirective(ownerId = userId, feedSpec = feedSpec)
        }

    private suspend fun NoteFeedRemoteMediator.syncFeed(
        loadType: LoadType,
        pagingState: PagingState<Int, FeedPost>,
        remoteKey: FeedPostRemoteKey?,
    ) {
        val pageSize = pagingState.config.pageSize
        val (request, response) = when (loadType) {
            LoadType.REFRESH -> syncRefresh(pageSize = pageSize)
            LoadType.PREPEND -> syncPrepend(remoteKey = remoteKey, pageSize = pageSize)
            LoadType.APPEND -> syncAppend(remoteKey = remoteKey, pageSize = pageSize)
        }

        feedProcessor.processAndPersistToDatabase(
            userId = userId,
            response = response,
            clearFeed = loadType == LoadType.REFRESH,
        )

        lastRequests[loadType] = request to Clock.System.now().epochSeconds
    }

    private suspend fun syncRefresh(pageSize: Int): Pair<FeedBySpecRequestBody, FeedResponse> {
        val requestBody = FeedBySpecRequestBody(
            spec = feedSpec,
            userPubKey = userId,
            limit = pageSize,
        )
        val response = retryNetworkCall(
            onBeforeDelay = { error -> Napier.w("Attempting FeedRemoteMediator.retry().", error) },
        ) {
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeedBySpec(body = requestBody) }
            response.paging ?: throw WssException("PagingEvent not found.")
            response
        }
        return requestBody to response
    }

    private suspend fun syncPrepend(
        remoteKey: FeedPostRemoteKey?,
        pageSize: Int,
    ): Pair<FeedBySpecRequestBody, FeedResponse> {
        val requestBody = FeedBySpecRequestBody(
            spec = feedSpec,
            userPubKey = userId,
            limit = pageSize,
            since = remoteKey?.untilId,
            order = "asc",
        )

        lastRequests[LoadType.PREPEND]?.let { (lastRequest, lastRequestAt) ->
            if (lastRequest == requestBody && lastRequestAt.isRequestCacheExpired()) {
                throw RepeatingRequestBodyException()
            }
        }

        val feedResponse = retryNetworkCall(
            onBeforeDelay = { error -> Napier.w("Attempting FeedRemoteMediator.retry().", error) },
        ) {
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeedBySpec(body = requestBody) }
            if (response.paging == null) throw WssException("PagingEvent not found.")
            response
        }

        return requestBody to feedResponse
    }

    private suspend fun syncAppend(
        remoteKey: FeedPostRemoteKey?,
        pageSize: Int,
    ): Pair<FeedBySpecRequestBody, FeedResponse> {
        val requestBody = FeedBySpecRequestBody(
            spec = feedSpec,
            userPubKey = userId,
            limit = pageSize,
            until = remoteKey?.sinceId,
        )

        lastRequests[LoadType.APPEND]?.let { (lastRequest, lastRequestAt) ->
            if (lastRequest == requestBody && lastRequestAt.isRequestCacheExpired()) {
                throw RepeatingRequestBodyException()
            }
        }

        val feedResponse = retryNetworkCall(
            onBeforeDelay = { error -> Napier.w("Attempting FeedRemoteMediator.retry().", error) },
        ) {
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeedBySpec(body = requestBody) }
            if (response.paging == null) throw WssException("PagingEvent not found.")
            response
        }

        return requestBody to feedResponse
    }

    private fun Long.isRequestCacheExpired() = (Clock.System.now().epochSeconds - this) < LAST_REQUEST_EXPIRY

//    private suspend fun findFirstFeedPostRemoteKey(state: PagingState<Int, FeedPost>): FeedPostRemoteKey? {
//        val firstItem = state.firstItemOrNull()
//            ?: newestFeedPostInDatabaseOrNull()
//            ?: throw NoSuchFeedPostException()
//
//        return withContext(dispatcherProvider.io()) {
//            Napier.i(
//                "feed_spec $feedDirective looking for firstItem postId=${firstItem.data.postId}" +
//                    " and repostId=${firstItem.data.repostId}",
//            )
//            database.feedPostsRemoteKeys().find(
//                postId = firstItem.data.postId,
//                repostId = firstItem.data.repostId,
//                directive = feedDirective,
//            )
//        }
//    }

    private suspend fun findLastFeedPostRemoteKey(state: PagingState<Int, FeedPost>): FeedPostRemoteKey? {
        val lastItem = state.lastItemOrNull()
            ?: oldestFeedPostInDatabaseOrNull()
            ?: throw NoSuchFeedPostException()

        return withContext(dispatcherProvider.io()) {
            Napier.i(
                "feed_spec $feedSpec looking for lastItem postId=${lastItem.data.postId}" +
                    " and repostId=${lastItem.data.repostId}",
            )
            database.feedPostsRemoteKeys().find(
                ownerId = userId,
                postId = lastItem.data.postId,
                repostId = lastItem.data.repostId,
                directive = feedSpec,
            )
        }
    }

    private suspend fun oldestFeedPostInDatabaseOrNull() =
        withContext(dispatcherProvider.io()) {
            database.feedPosts()
                .oldestFeedPosts(query = feedQueryBuilder.oldestFeedPostsQuery(limit = 1))
                .firstOrNull()
        }

    private inner class NoSuchFeedPostException : RuntimeException()

    private inner class RepeatingRequestBodyException : RuntimeException()

    private inner class RemoteKeyNotFoundException : RuntimeException()

    companion object {
        private val LAST_REQUEST_EXPIRY = 10.seconds.inWholeSeconds
    }
}
