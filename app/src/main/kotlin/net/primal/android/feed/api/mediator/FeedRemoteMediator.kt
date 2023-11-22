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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.core.ext.isLatestFeed
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.db.FeedPostDataCrossRef
import net.primal.android.feed.db.FeedPostRemoteKey
import net.primal.android.feed.db.FeedPostSync
import net.primal.android.feed.db.sql.ExploreFeedQueryBuilder
import net.primal.android.feed.db.sql.FeedQueryBuilder
import net.primal.android.feed.db.sql.LatestFeedQueryBuilder
import net.primal.android.feed.repository.persistToDatabaseAsTransaction
import net.primal.android.networking.sockets.errors.NostrNoticeException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

@ExperimentalPagingApi
class FeedRemoteMediator(
    private val feedDirective: String,
    private val userPubkey: String,
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, FeedPost>() {

    private val feedQueryBuilder: FeedQueryBuilder = when {
        feedDirective.isLatestFeed() -> LatestFeedQueryBuilder(
            feedDirective = feedDirective,
            userPubkey = userPubkey,
        )

        else -> ExploreFeedQueryBuilder(
            feedDirective = feedDirective,
            userPubkey = userPubkey,
        )
    }

    private var prependSyncCount = 0

    private fun FeedPost?.isOlderThan(duration: Duration): Boolean {
        if (this == null) return true
        val postFeedCreateAt = Instant.ofEpochSecond(this.data.feedCreatedAt)
        return postFeedCreateAt < Instant.now().minusSeconds(duration.inWholeSeconds)
    }

    private suspend fun shouldRefreshLatestFeed(): Boolean {
        val firstPost = withContext(Dispatchers.IO) {
            database.feedPosts().newestFeedPosts(query = feedQueryBuilder.feedQuery()).firstOrNull()
        }
        return firstPost.isOlderThan(2.days)
    }

    private suspend fun shouldRefreshNonLatestFeed(feedDirective: String): Boolean {
        val lastCachedAt = withContext(Dispatchers.IO) {
            database.feedPostsRemoteKeys().lastCachedAt(directive = feedDirective)
        } ?: return true

        return lastCachedAt < Instant.now().minusSeconds(30.minutes.inWholeSeconds).epochSecond
    }

    private suspend fun shouldResetLocalCache(feedDirective: String) =
        when {
            feedDirective.isLatestFeed() -> shouldRefreshLatestFeed()
            else -> shouldRefreshNonLatestFeed(feedDirective)
        }

    override suspend fun initialize(): InitializeAction {
        return when {
            shouldResetLocalCache(feedDirective) -> InitializeAction.LAUNCH_INITIAL_REFRESH
            else -> InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, FeedPost>): MediatorResult {
        return try {
            val remoteKey: FeedPostRemoteKey? = when (loadType) {
                LoadType.REFRESH -> {
                    val firstItem = state.firstItemOrNull()
                    val lastItem = state.lastItemOrNull()

                    if (firstItem != null || lastItem != null) {
                        if (shouldResetLocalCache(feedDirective)) {
                            withContext(Dispatchers.IO) {
                                database.withTransaction {
                                    database.feedPostsRemoteKeys().deleteByDirective(feedDirective)
                                    database.feedsConnections()
                                        .deleteConnectionsByDirective(feedDirective)
                                    database.posts().deleteOrphanPosts()
                                }
                            }
                        } else {
                            return MediatorResult.Success(endOfPaginationReached = false)
                        }
                    }

                    null
                }

                LoadType.PREPEND -> {
                    val firstItem = state.firstItemOrNull()
                        ?: withContext(Dispatchers.IO) {
                            database.feedPosts()
                                .newestFeedPosts(
                                    query = feedQueryBuilder.newestFeedPostsQuery(limit = 1),
                                )
                                .firstOrNull()
                        }
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = true,
                        )

                    withContext(Dispatchers.IO) {
                        database.feedPostsRemoteKeys().find(
                            postId = firstItem.data.postId,
                            repostId = firstItem.data.repostId,
                            directive = feedDirective,
                        )
                    }
                }

                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: withContext(Dispatchers.IO) {
                            database.feedPosts()
                                .oldestFeedPosts(
                                    query = feedQueryBuilder.oldestFeedPostsQuery(limit = 1),
                                )
                                .firstOrNull()
                        }
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = true,
                        )

                    withContext(Dispatchers.IO) {
                        database.feedPostsRemoteKeys().find(
                            postId = lastItem.data.postId,
                            repostId = lastItem.data.repostId,
                            directive = feedDirective,
                        )
                    }
                }
            }

            val initialRequestBody = FeedRequestBody(
                directive = feedDirective,
                userPubKey = userPubkey,
                limit = state.config.pageSize,
            )

            if (remoteKey == null && loadType != LoadType.REFRESH) {
                return MediatorResult.Error(IllegalStateException("Remote key not found."))
            }

            val feedRequestBody = when (loadType) {
                LoadType.REFRESH -> initialRequestBody
                LoadType.PREPEND -> initialRequestBody.copy(since = remoteKey?.untilId)
                LoadType.APPEND -> initialRequestBody.copy(until = remoteKey?.sinceId)
            }

            val feedResponse = try {
                withContext(Dispatchers.IO) { feedApi.getFeed(body = feedRequestBody) }
            } catch (error: WssException) {
                return MediatorResult.Error(error)
            }

            val pagingEvent = feedResponse.paging
            if (pagingEvent?.untilId == pagingEvent?.sinceId) {
                if (loadType == LoadType.PREPEND) {
                    if (prependSyncCount > 1) {
                        withContext(Dispatchers.IO) {
                            database.withTransaction {
                                val actualCount = prependSyncCount - 1
                                val postIds = database.feedPosts().newestFeedPosts(
                                    query = feedQueryBuilder.newestFeedPostsQuery(
                                        limit = actualCount,
                                    ),
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

                return MediatorResult.Success(endOfPaginationReached = true)
            } else {
                if (loadType == LoadType.PREPEND) {
                    prependSyncCount += feedResponse.posts.size + feedResponse.reposts.size
                }
            }

            withContext(Dispatchers.IO) {
                feedResponse.persistToDatabaseAsTransaction(
                    userId = userPubkey,
                    database = database,
                )
                val feedEvents = feedResponse.posts + feedResponse.reposts
                feedEvents.processRemoteKeys(pagingEvent)
                feedEvents.processFeedConnections()
            }

            MediatorResult.Success(endOfPaginationReached = false)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: NostrNoticeException) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun List<NostrEvent>.processRemoteKeys(pagingEvent: ContentPrimalPaging?) {
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
}
