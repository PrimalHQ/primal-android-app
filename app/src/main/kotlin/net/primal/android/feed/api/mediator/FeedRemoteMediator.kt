package net.primal.android.feed.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
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
import net.primal.android.networking.sockets.NostrNoticeException
import net.primal.android.nostr.ext.asEventStatsPO
import net.primal.android.nostr.ext.asEventUserStatsPO
import net.primal.android.nostr.ext.asPost
import net.primal.android.nostr.ext.asPostResourcePO
import net.primal.android.nostr.ext.flatMapAsPostResources
import net.primal.android.nostr.ext.mapAsProfileMetadata
import net.primal.android.nostr.ext.mapNotNullAsPost
import net.primal.android.nostr.ext.mapNotNullAsRepost
import net.primal.android.nostr.ext.takeContentAsNostrEventOrNull
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventResources
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats
import net.primal.android.nostr.model.primal.content.ContentPrimalEventUserStats
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging
import net.primal.android.serialization.NostrJson
import timber.log.Timber
import java.io.IOException
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

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
        return firstPost.isOlderThan(3.days)
    }

    private suspend fun shouldRefreshNonLatestFeed(feedDirective: String): Boolean {
        val lastCachedAt = withContext(Dispatchers.IO) {
            database.feedPostsRemoteKeys().lastCachedAt(directive = feedDirective)
        } ?: return true

        return lastCachedAt < Instant.now().minusSeconds(30.minutes.inWholeSeconds).epochSecond
    }

    private suspend fun shouldResetLocalCache(feedDirective: String) = when {
        feedDirective.isLatestFeed() -> shouldRefreshLatestFeed()
        else -> shouldRefreshNonLatestFeed(feedDirective)
    }

    override suspend fun initialize(): InitializeAction {
        return when {
            shouldResetLocalCache(feedDirective) -> InitializeAction.LAUNCH_INITIAL_REFRESH
            else -> InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FeedPost>
    ): MediatorResult {
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
                                    query = feedQueryBuilder.newestFeedPostsQuery(limit = 1)
                                )
                                .firstOrNull()
                        }
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = true
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
                                    query = feedQueryBuilder.oldestFeedPostsQuery(limit = 1)
                                )
                                .firstOrNull()
                        }
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = true
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

            val response = withContext(Dispatchers.IO) {
                feedApi.getFeed(body = feedRequestBody)
            }

            val pagingEvent = response.paging
            if (pagingEvent?.untilId == pagingEvent?.sinceId) {
                if (loadType == LoadType.PREPEND) {
                    if (prependSyncCount > 1) {
                        withContext(Dispatchers.IO) {
                            database.withTransaction {
                                val actualCount = prependSyncCount - 1
                                val postIds = database.feedPosts().newestFeedPosts(
                                    query = feedQueryBuilder.newestFeedPostsQuery(
                                        limit = actualCount
                                    )
                                ).map { it.data.postId }

                                database.feedPostsSync().upsert(
                                    data = FeedPostSync(
                                        timestamp = Instant.now().epochSecond,
                                        feedDirective = feedDirective,
                                        count = actualCount,
                                        postIds = postIds,
                                    )
                                )
                            }
                        }
                    }
                    prependSyncCount = 0
                }

                return MediatorResult.Success(endOfPaginationReached = true)
            } else {
                if (loadType == LoadType.PREPEND) {
                    prependSyncCount += response.posts.size + response.reposts.size
                }
            }

            withContext(Dispatchers.IO) {
                (response.posts + response.reposts).processRemoteKeys(pagingEvent)

                response.metadata.processMetadataEvents()
                processShortTextNotesAndReposts(
                    feedDirective = feedDirective,
                    postEvents = response.posts,
                    repostEvents = response.reposts,
                )

                response.referencedPosts.processReferencedEvents()
                response.primalEventStats.processEventStats()
                response.primalEventUserStats.processEventUserStats()
                response.primalEventResources.processEventResources()
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

    private suspend fun processShortTextNotesAndReposts(
        feedDirective: String,
        postEvents: List<NostrEvent>,
        repostEvents: List<NostrEvent>,
    ) {
        database.withTransaction {
            val posts = postEvents.mapNotNullAsPost()
            val reposts = repostEvents.mapNotNullAsRepost()
            database.posts().upsertAll(data = posts)
            database.reposts().upsertAll(data = reposts)

            val feedConnections = posts.map { it.postId } + reposts.map { it.repostId }

            database.feedsConnections().connect(
                data = feedConnections.map { postId ->
                    FeedPostDataCrossRef(
                        feedDirective = feedDirective,
                        eventId = postId
                    )
                }
            )

            database.resources().upsert(data = posts.flatMapAsPostResources())
            Timber.i("Received ${posts.size} posts and ${reposts.size} reposts..")
        }
    }

    private fun List<NostrEvent>.processMetadataEvents() {
        database.profiles().upsertAll(events = mapAsProfileMetadata())
    }

    private fun List<PrimalEvent>.processReferencedEvents() {
        database.posts().upsertAll(
            data = this
                .mapNotNull { it.takeContentAsNostrEventOrNull() }
                .map { it.asPost() }
        )
    }

    private fun List<PrimalEvent>.processEventStats() {
        database.postStats().upsertAll(
            data = this
                .map { NostrJson.decodeFromString<ContentPrimalEventStats>(it.content) }
                .map { it.asEventStatsPO() }
        )
    }

    private fun List<PrimalEvent>.processEventUserStats() {
        database.postUserStats().upsertAll(
            data = this
                .map { NostrJson.decodeFromString<ContentPrimalEventUserStats>(it.content) }
                .map { it.asEventUserStatsPO(userId = userPubkey) }
        )
    }

    private fun List<PrimalEvent>.processEventResources() {
        database.resources().upsert(
            data = this
                .map { NostrJson.decodeFromString<ContentPrimalEventResources>(it.content) }
                .flatMap {
                    val eventId = it.eventId
                    it.resources.map { eventResource ->
                        eventResource.asPostResourcePO(postId = eventId)
                    }
                }
        )
    }

}
