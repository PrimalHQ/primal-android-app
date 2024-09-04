package net.primal.android.notes.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import java.io.IOException
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.hasReposts
import net.primal.android.core.ext.hasUpwardsPagination
import net.primal.android.core.ext.isBookmarkFeed
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.sockets.errors.NostrNoticeException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.notes.api.FeedApi
import net.primal.android.notes.api.model.FeedRequestBody
import net.primal.android.notes.api.model.FeedResponse
import net.primal.android.notes.db.FeedPost
import net.primal.android.notes.db.FeedPostRemoteKey
import net.primal.android.notes.db.sql.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.android.notes.db.sql.ExploreFeedQueryBuilder
import net.primal.android.notes.db.sql.FeedQueryBuilder
import net.primal.android.notes.repository.FeedProcessor
import timber.log.Timber

@ExperimentalPagingApi
class FeedRemoteMediator(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedDirective: String,
    private val userId: String,
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, FeedPost>() {

    private val feedQueryBuilder: FeedQueryBuilder = when {
        feedDirective.hasReposts() -> ChronologicalFeedWithRepostsQueryBuilder(
            feedDirective = feedDirective,
            userPubkey = userId,
        )

        else -> ExploreFeedQueryBuilder(
            feedDirective = feedDirective,
            userPubkey = userId,
        )
    }

    private val lastRequests: MutableMap<LoadType, Pair<FeedRequestBody, Long>> = mutableMapOf()

    private val feedProcessor: FeedProcessor = FeedProcessor(feedDirective = feedDirective, database = database)

    private fun FeedPost?.isOlderThan(duration: Duration): Boolean {
        if (this == null) return true
        val postFeedCreateAt = Instant.ofEpochSecond(this.data.feedCreatedAt)
        return postFeedCreateAt < Instant.now().minusSeconds(duration.inWholeSeconds)
    }

    private suspend fun shouldRefreshLatestFeed(): Boolean {
        return newestFeedPostInDatabaseOrNull().isOlderThan(1.days)
    }

    private suspend fun shouldRefreshNonLatestFeed(feedDirective: String): Boolean {
        val lastCachedAt = withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys().lastCachedAt(directive = feedDirective)
        } ?: return true

        return lastCachedAt < Instant.now().minusSeconds(3.minutes.inWholeSeconds).epochSecond
    }

    private suspend fun shouldResetLocalCache() =
        when {
            feedDirective.hasUpwardsPagination() -> shouldRefreshLatestFeed()
            feedDirective.isBookmarkFeed() -> true
            else -> shouldRefreshNonLatestFeed(feedDirective)
        }

    override suspend fun initialize(): InitializeAction {
        return when {
            shouldResetLocalCache() -> InitializeAction.LAUNCH_INITIAL_REFRESH
            else -> InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override suspend fun load(loadType: LoadType, state: PagingState<Int, FeedPost>): MediatorResult {
        Timber.i("feed_directive $feedDirective load called ($loadType)")
        if (loadType == LoadType.PREPEND) {
            Timber.w("feed_directive $feedDirective load exit 9")
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

            Timber.w("feed_directive $feedDirective load exit 6")
            MediatorResult.Success(endOfPaginationReached = false)
        } catch (error: IOException) {
            Timber.w(error, "feed_directive $feedDirective load exit 7")
            MediatorResult.Error(error)
        } catch (error: NostrNoticeException) {
            Timber.w(error, "feed_directive $feedDirective load exit 8")
            MediatorResult.Error(error)
        } catch (error: NoSuchFeedPostException) {
            Timber.w(error, "feed_directive $feedDirective load exit 2")
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (error: RemoteKeyNotFoundException) {
            Timber.w(error, "feed_directive $feedDirective load exit 3")
            MediatorResult.Error(error)
        } catch (error: WssException) {
            Timber.w(error, "feed_directive $feedDirective load exit 5")
            MediatorResult.Error(error)
        } catch (error: RepeatingRequestBodyException) {
            Timber.w(error, "feed_directive $feedDirective load exit 4")
            MediatorResult.Success(endOfPaginationReached = true)
        }
    }

    private suspend fun FeedRemoteMediator.syncFeed(
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

        lastRequests[loadType] = request to Instant.now().epochSecond
    }

    private suspend fun syncRefresh(pageSize: Int): Pair<FeedRequestBody, FeedResponse> {
        val requestBody = FeedRequestBody(
            directive = feedDirective,
            userPubKey = userId,
            limit = pageSize,
        )
        val response = retry(times = 1, delay = RETRY_DELAY) {
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeedMega(body = requestBody) }
            response.paging ?: throw WssException("PagingEvent not found.")
            response
        }
        return requestBody to response
    }

    private suspend fun syncPrepend(remoteKey: FeedPostRemoteKey?, pageSize: Int): Pair<FeedRequestBody, FeedResponse> {
        val requestBody = FeedRequestBody(
            directive = feedDirective,
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

        val feedResponse = retry(times = 1, delay = RETRY_DELAY) {
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeedMega(body = requestBody) }
            if (response.paging == null) throw WssException("PagingEvent not found.")
            response
        }

        return requestBody to feedResponse
    }

    private suspend fun syncAppend(remoteKey: FeedPostRemoteKey?, pageSize: Int): Pair<FeedRequestBody, FeedResponse> {
        val requestBody = FeedRequestBody(
            directive = feedDirective,
            userPubKey = userId,
            limit = pageSize,
            until = remoteKey?.sinceId,
        )

        lastRequests[LoadType.APPEND]?.let { (lastRequest, lastRequestAt) ->
            if (lastRequest == requestBody && lastRequestAt.isRequestCacheExpired()) {
                throw RepeatingRequestBodyException()
            }
        }

        val feedResponse = retry(times = 1, delay = RETRY_DELAY) {
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeedMega(body = requestBody) }
            if (response.paging == null) throw WssException("PagingEvent not found.")
            response
        }

        return requestBody to feedResponse
    }

    private fun Long.isRequestCacheExpired() = (Instant.now().epochSecond - this) < LAST_REQUEST_EXPIRY

    private suspend fun <T> retry(
        times: Int,
        delay: Long,
        block: suspend () -> T,
    ): T {
        repeat(times) {
            try {
                Timber.i("Executing retry $it.")
                return block()
            } catch (error: WssException) {
                Timber.w(error, "Attempting FeedRemoteMediator.retry() in $delay millis.")
                delay(delay)
            }
        }
        return block()
    }

//    private suspend fun findFirstFeedPostRemoteKey(state: PagingState<Int, FeedPost>): FeedPostRemoteKey? {
//        val firstItem = state.firstItemOrNull()
//            ?: newestFeedPostInDatabaseOrNull()
//            ?: throw NoSuchFeedPostException()
//
//        return withContext(dispatcherProvider.io()) {
//            Timber.i(
//                "feed_directive $feedDirective looking for firstItem postId=${firstItem.data.postId}" +
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
            Timber.i(
                "feed_directive $feedDirective looking for lastItem postId=${lastItem.data.postId}" +
                    " and repostId=${lastItem.data.repostId}",
            )
            database.feedPostsRemoteKeys().find(
                postId = lastItem.data.postId,
                repostId = lastItem.data.repostId,
                directive = feedDirective,
            )
        }
    }

    private suspend fun oldestFeedPostInDatabaseOrNull() =
        withContext(dispatcherProvider.io()) {
            database.feedPosts()
                .oldestFeedPosts(query = feedQueryBuilder.oldestFeedPostsQuery(limit = 1))
                .firstOrNull()
        }

    private suspend fun newestFeedPostInDatabaseOrNull() =
        withContext(dispatcherProvider.io()) {
            database.feedPosts()
                .newestFeedPosts(query = feedQueryBuilder.newestFeedPostsQuery(limit = 1))
                .firstOrNull()
        }

    private inner class NoSuchFeedPostException : RuntimeException()

    private inner class RepeatingRequestBodyException : RuntimeException()

    private inner class RemoteKeyNotFoundException : RuntimeException()

    companion object {
        private val LAST_REQUEST_EXPIRY = 10.seconds.inWholeSeconds
        private val RETRY_DELAY = 500.milliseconds.inWholeMilliseconds
    }
}
