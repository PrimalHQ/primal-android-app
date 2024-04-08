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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.isBookmarkFeed
import net.primal.android.core.ext.isChronologicalFeed
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.db.FeedPostDataCrossRef
import net.primal.android.feed.db.FeedPostRemoteKey
import net.primal.android.feed.db.FeedPostSync
import net.primal.android.feed.db.sql.ChronologicalFeedQueryBuilder
import net.primal.android.feed.db.sql.ExploreFeedQueryBuilder
import net.primal.android.feed.db.sql.FeedQueryBuilder
import net.primal.android.feed.repository.persistToDatabaseAsTransaction
import net.primal.android.networking.sockets.errors.NostrNoticeException
import net.primal.android.networking.sockets.errors.WssException
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
        feedDirective.isChronologicalFeed() -> ChronologicalFeedQueryBuilder(
            feedDirective = feedDirective,
            userPubkey = userId,
        )

        else -> ExploreFeedQueryBuilder(
            feedDirective = feedDirective,
            userPubkey = userId,
        )
    }

    private var prependSyncCount = 0

    private val lastRequests: MutableMap<LoadType, Pair<FeedRequestBody, Long>> = mutableMapOf()

    private fun FeedPost?.isOlderThan(duration: Duration): Boolean {
        if (this == null) return true
        val postFeedCreateAt = Instant.ofEpochSecond(this.data.feedCreatedAt)
        return postFeedCreateAt < Instant.now().minusSeconds(duration.inWholeSeconds)
    }

    private suspend fun shouldRefreshLatestFeed(): Boolean {
        return newestFeedPostInDatabaseOrNull().isOlderThan(2.days)
    }

    private suspend fun shouldRefreshNonLatestFeed(feedDirective: String): Boolean {
        val lastCachedAt = withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys().lastCachedAt(directive = feedDirective)
        } ?: return true

        return lastCachedAt < Instant.now().minusSeconds(3.minutes.inWholeSeconds).epochSecond
    }

    private suspend fun shouldResetLocalCache() =
        when {
            feedDirective.isChronologicalFeed() -> shouldRefreshLatestFeed()
            feedDirective.isBookmarkFeed() -> true
            else -> shouldRefreshNonLatestFeed(feedDirective)
        }

    override suspend fun initialize(): InitializeAction {
        return when {
            shouldResetLocalCache() -> InitializeAction.LAUNCH_INITIAL_REFRESH
            else -> InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, FeedPost>): MediatorResult {
        Timber.i("feed_directive $feedDirective load called ($loadType)")
        return try {
            val remoteKey = try {
                when (loadType) {
                    LoadType.REFRESH -> null
                    LoadType.PREPEND -> findFirstFeedPostRemoteKey(state = state)
                    LoadType.APPEND -> findLastFeedPostRemoteKey(state = state)
                }
            } catch (error: NoSuchFeedPostException) {
                Timber.w(error)
                Timber.w("feed_directive $feedDirective load exit 2")
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            if (remoteKey == null && loadType != LoadType.REFRESH) {
                val error = IllegalStateException("Remote key not found.")
                Timber.w(error)
                Timber.w("feed_directive $feedDirective load exit 3")
                return MediatorResult.Error(error)
            }

            val requestBody = buildFeedRequestBody(
                loadType = loadType,
                remoteKey = remoteKey,
                pageSize = state.config.pageSize,
            )

            lastRequests[loadType]?.let { (lastRequest, lastRequestAt) ->
                if (lastRequest == requestBody && lastRequestAt.isRequestCacheExpired()) {
                    Timber.e("feed_directive $feedDirective paging exiting because of repeating request body.")
                    Timber.w("feed_directive $feedDirective load exit 4")
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            if (loadType == LoadType.REFRESH) {
                if (state.hasFeedPosts()) {
                    if (!shouldResetLocalCache()) {
                        Timber.w("feed_directive $feedDirective load exit 1")
                        return MediatorResult.Success(endOfPaginationReached = false)
                    }
                }
            }

            val feedResponse = try {
                retry(times = 1, delay = 500L) {
                    val response = withContext(dispatcherProvider.io()) { feedApi.getFeed(body = requestBody) }
                    if (response.paging == null) throw WssException("PagingEvent not found.")
                    response
                }
            } catch (error: WssException) {
                Timber.w(error)
                Timber.w("feed_directive $feedDirective load exit 5")
                return MediatorResult.Error(error)
            }

            val pagingEvent = feedResponse.paging
            Timber.w("feed_directive $feedDirective paging event = $pagingEvent")
            if (pagingEvent?.untilId == pagingEvent?.sinceId) {
                if (loadType == LoadType.PREPEND) {
                    // Prepend and append syncs always include and the last known item
                    if (prependSyncCount > 1) {
                        withContext(dispatcherProvider.io()) {
                            database.withTransaction {
                                val actualCount = prependSyncCount - 1
                                val postIds = database.feedPosts().newestFeedPosts(
                                    query = feedQueryBuilder.newestFeedPostsQuery(limit = actualCount),
                                ).map { it.data.postId }

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
                    prependSyncCount = 0
                }
            } else {
                if (loadType == LoadType.PREPEND) {
                    prependSyncCount += feedResponse.posts.size + feedResponse.reposts.size
                }
            }

            val shouldDeleteLocalData = loadType == LoadType.REFRESH && state.hasFeedPosts() && shouldResetLocalCache()
            withContext(dispatcherProvider.io()) {
                database.withTransaction {
                    if (shouldDeleteLocalData) {
                        database.feedPostsRemoteKeys().deleteByDirective(feedDirective)
                        database.feedsConnections().deleteConnectionsByDirective(feedDirective)
                        database.posts().deleteOrphanPosts()
                    }

                    feedResponse.persistToDatabaseAsTransaction(userId = userId, database = database)
                    val feedEvents = feedResponse.posts + feedResponse.reposts
                    feedEvents.processRemoteKeys(pagingEvent)
                    feedEvents.processFeedConnections()
                }
            }

            lastRequests[loadType] = requestBody to Instant.now().epochSecond
            Timber.w("feed_directive $feedDirective load exit 6")
            MediatorResult.Success(endOfPaginationReached = false)
        } catch (error: IOException) {
            Timber.w(error)
            Timber.w("feed_directive $feedDirective load exit 7")
            MediatorResult.Error(error)
        } catch (error: NostrNoticeException) {
            Timber.w(error)
            Timber.w("feed_directive $feedDirective load exit 8")
            MediatorResult.Error(error)
        }
    }

    private fun Long.isRequestCacheExpired() = (Instant.now().epochSecond - this) < LAST_REQUEST_EXPIRY

    private fun buildFeedRequestBody(
        loadType: LoadType,
        remoteKey: FeedPostRemoteKey?,
        pageSize: Int,
    ): FeedRequestBody {
        val initialRequestBody = FeedRequestBody(
            directive = feedDirective,
            userPubKey = userId,
            limit = pageSize,
        )
        return when (loadType) {
            LoadType.REFRESH -> initialRequestBody
            LoadType.PREPEND -> initialRequestBody.copy(since = remoteKey?.untilId)
            LoadType.APPEND -> initialRequestBody.copy(until = remoteKey?.sinceId)
        }
    }

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
                Timber.w(error, "FeedRemoteMediator.retry()")
                delay(delay)
            }
        }
        return block()
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

    class NoSuchFeedPostException : RuntimeException()

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

    companion object {
        val LAST_REQUEST_EXPIRY = 10.seconds.inWholeSeconds
    }
}
