package net.primal.android.feed.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
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
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.db.FeedPostDataCrossRef
import net.primal.android.feed.db.FeedPostRemoteKey
import net.primal.android.feed.db.FeedPostSync
import net.primal.android.feed.db.sql.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.android.feed.db.sql.ExploreFeedQueryBuilder
import net.primal.android.feed.db.sql.FeedQueryBuilder
import net.primal.android.feed.repository.persistToDatabaseAsTransaction
import net.primal.android.networking.sockets.errors.NostrNoticeException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.findFirstEventId
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging
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
        return try {
            if (loadType == LoadType.REFRESH && state.hasFeedPosts() && !shouldResetLocalCache()) {
                throw UnnecessaryRefreshSync()
            }

            val remoteKey = when (loadType) {
                LoadType.PREPEND -> findFirstFeedPostRemoteKey(state = state)
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
        } catch (error: UnnecessaryRefreshSync) {
            Timber.w(error, "feed_directive $feedDirective load exit 1")
            MediatorResult.Success(endOfPaginationReached = false)
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
            LoadType.PREPEND -> syncPrepend(remoteKey = remoteKey)
            LoadType.APPEND -> syncAppend(remoteKey = remoteKey, pageSize = pageSize)
        }

        val pagingEvent = response.paging
        val shouldDeleteLocalData = loadType == LoadType.REFRESH &&
            pagingState.hasFeedPosts() && shouldResetLocalCache()

        database.withTransaction {
            if (shouldDeleteLocalData) {
                database.feedPostsRemoteKeys().deleteByDirective(feedDirective)
                database.feedsConnections().deleteConnectionsByDirective(feedDirective)
                database.posts().deleteOrphanPosts()
            }

            response.persistToDatabaseAsTransaction(userId = userId, database = database)
            val feedEvents = response.posts + response.reposts
            feedEvents.processRemoteKeys(pagingEvent)
            feedEvents.processFeedConnections()
        }

        if (loadType == LoadType.PREPEND) {
            response.processSyncCount()
        }

        lastRequests[loadType] = request to Instant.now().epochSecond
    }

    private suspend fun syncRefresh(pageSize: Int): Pair<FeedRequestBody, FeedResponse> {
        val requestBody = FeedRequestBody(
            directive = feedDirective,
            userPubKey = userId,
            limit = pageSize,
        )
        val response = retry(times = 1, delay = RETRY_DELAY) {
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeed(body = requestBody) }
            response.paging ?: throw WssException("PagingEvent not found.")
            response
        }
        return requestBody to response
    }

    private suspend fun syncPrepend(remoteKey: FeedPostRemoteKey?): Pair<FeedRequestBody, FeedResponse> {
        val requestBody = FeedRequestBody(
            directive = feedDirective,
            userPubKey = userId,
            limit = 500,
            since = remoteKey?.untilId,
            until = Instant.now().epochSecond,
            order = "asc",
        )

        lastRequests[LoadType.PREPEND]?.let { (lastRequest, lastRequestAt) ->
            if (lastRequest == requestBody && lastRequestAt.isRequestCacheExpired()) {
                throw RepeatingRequestBodyException()
            }
        }

        val feedResponse = retry(times = 1, delay = RETRY_DELAY) {
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeed(body = requestBody) }
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
            val response = withContext(dispatcherProvider.io()) { feedApi.getFeed(body = requestBody) }
            if (response.paging == null) throw WssException("PagingEvent not found.")
            response
        }

        return requestBody to feedResponse
    }

    private suspend fun FeedResponse.processSyncCount() {
        val prependSyncCount = this.posts.size + this.reposts.size

        val repostedNoteIds = this.reposts
            .sortedByDescending { it.createdAt }
            .mapNotNull { it.tags.findFirstEventId() }

        val noteIds = this.posts
            .sortedByDescending { it.createdAt }
            .map { it.id }

        // Prepend syncs always include and the last known item
        if (prependSyncCount > 1) {
            withContext(dispatcherProvider.io()) {
                database.withTransaction {
                    val actualCount = prependSyncCount - 1
                    val postIds = repostedNoteIds + noteIds
                    database.feedPostsSync().upsert(
                        data = FeedPostSync(
                            timestamp = Instant.now().epochSecond,
                            feedDirective = feedDirective,
                            count = actualCount,
                            postIds = postIds,
                        ),
                    )
                }
            }
        }
    }

    private suspend fun List<NostrEvent>.processRemoteKeys(pagingEvent: ContentPrimalPaging?) {
        Timber.i("feed_directive $feedDirective writing remote keys using $pagingEvent")
        if (pagingEvent?.sinceId != null && pagingEvent.untilId != null) {
            database.withTransaction {
                val remoteKeys = this.map {
                    FeedPostRemoteKey(
                        eventId = it.id,
                        directive = feedDirective,
                        sinceId = pagingEvent.sinceId,
                        untilId = pagingEvent.untilId,
                        cachedAt = Instant.now().epochSecond,
                    )
                }

                database.feedPostsRemoteKeys().upsert(remoteKeys)
            }
        }
    }

    private fun List<NostrEvent>.processFeedConnections() {
        database.feedsConnections().connect(
            data = this.map {
                FeedPostDataCrossRef(
                    feedDirective = feedDirective,
                    eventId = it.id,
                )
            },
        )
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

    private suspend fun findFirstFeedPostRemoteKey(state: PagingState<Int, FeedPost>): FeedPostRemoteKey? {
        val firstItem = state.firstItemOrNull()
            ?: newestFeedPostInDatabaseOrNull()
            ?: throw NoSuchFeedPostException()

        return withContext(dispatcherProvider.io()) {
            Timber.i(
                "feed_directive $feedDirective looking for firstItem postId=${firstItem.data.postId}" +
                    " and repostId=${firstItem.data.repostId}",
            )
            database.feedPostsRemoteKeys().find(
                postId = firstItem.data.postId,
                repostId = firstItem.data.repostId,
                directive = feedDirective,
            )
        }
    }

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

    private fun PagingState<Int, FeedPost>.hasFeedPosts() = firstItemOrNull() != null || lastItemOrNull() != null

    private inner class NoSuchFeedPostException : RuntimeException()

    private inner class RepeatingRequestBodyException : RuntimeException()

    private inner class RemoteKeyNotFoundException : RuntimeException()

    private inner class UnnecessaryRefreshSync : RuntimeException()

    companion object {
        private val LAST_REQUEST_EXPIRY = 10.seconds.inWholeSeconds
        private val RETRY_DELAY = 500.milliseconds.inWholeMilliseconds
    }
}
