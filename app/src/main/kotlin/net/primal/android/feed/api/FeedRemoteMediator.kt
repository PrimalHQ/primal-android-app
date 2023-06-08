package net.primal.android.feed.api

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.db.FeedPostDataCrossRef
import net.primal.android.feed.db.FeedPostRemoteKey
import net.primal.android.feed.isLatestFeed
import net.primal.android.feed.isNotLatestFeed
import net.primal.android.networking.sockets.NostrNoticeException
import net.primal.android.nostr.ext.asEventStatsPO
import net.primal.android.nostr.ext.asPostResourcePO
import net.primal.android.nostr.ext.flatMapAsPostResources
import net.primal.android.nostr.ext.mapAsProfileMetadata
import net.primal.android.nostr.ext.mapNotNullAsPost
import net.primal.android.nostr.ext.mapNotNullAsRepost
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventResources
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats
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
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, FeedPost>() {

    private fun Long?.isOlderThan(duration: Duration): Boolean {
        if (this == null) return true
        val postFeedCreateAt = Instant.ofEpochSecond(this)
        return postFeedCreateAt < Instant.now().minusSeconds(duration.inWholeSeconds)
    }

    private suspend fun shouldRefreshLatestFeed(feedDirective: String): Boolean {
        val newestPostFeedCreatedAt = withContext(Dispatchers.IO) {
            database.feedPosts().newestPostFeedCreatedAt(feedDirective = feedDirective)
        }
        return newestPostFeedCreatedAt.isOlderThan(2.days)
    }

    private suspend fun shouldRefreshNonLatestFeed(feedDirective: String): Boolean {
        val lastCachedAt = withContext(Dispatchers.IO) {
            database.feedPostsRemoteKeys().lastCachedAt(directive = feedDirective)
        }
        return lastCachedAt < Instant.now().minusSeconds(30.minutes.inWholeSeconds).epochSecond
    }

    override suspend fun initialize(): InitializeAction = when {
        feedDirective.isLatestFeed() && shouldRefreshLatestFeed(feedDirective) -> {
            Timber.i("Launching initial refresh for latest feed.")
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }

        feedDirective.isNotLatestFeed() && shouldRefreshNonLatestFeed(feedDirective) -> {
            Timber.i("Launching initial refresh for other feed.")
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }

        else -> {
            Timber.i("Skipping initial refresh.")
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FeedPost>
    ): MediatorResult {
        Timber.i("load(loadType=$loadType, state)")
        return try {
            val remoteKey = when (loadType) {
                LoadType.REFRESH -> null

                LoadType.PREPEND -> {
                    val firstItem = state.firstItemOrNull()
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
                // User hard-coded to Nostr Highlights
                userPubKey = "9a500dccc084a138330a1d1b2be0d5e86394624325d25084d3eca164e7ea698a",
                limit = state.config.pageSize,
            )

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
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            withContext(Dispatchers.IO) {
                if (pagingEvent?.sinceId != null && pagingEvent.untilId != null) {
                    val remoteKeys = (response.posts + response.reposts)
                        .map {
                            FeedPostRemoteKey(
                                eventId = it.id,
                                directive = feedDirective,
                                sinceId = pagingEvent.sinceId,
                                untilId = pagingEvent.untilId,
                                cachedAt = Instant.now().epochSecond,
                            )
                        }

                    database.withTransaction {
                        if (loadType == LoadType.REFRESH) {
                            database.feedPostsRemoteKeys().deleteByDirective(
                                directive = feedDirective
                            )
                        }
                        database.feedPostsRemoteKeys().upsert(remoteKeys)
                    }
                }

                database.withTransaction {
                    processShortTextNotesAndReposts(
                        feedDirective = feedDirective,
                        shortTextNoteEvents = response.posts + response.referencedPosts,
                        repostEvents = response.reposts,
                    )
                    response.metadata.processMetadataEvents()
                    response.primalEventStats.processEventStats()
                    response.primalEventResources.processEventResources()
                }
            }

            return MediatorResult.Success(endOfPaginationReached = false)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: NostrNoticeException) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun processShortTextNotesAndReposts(
        feedDirective: String,
        shortTextNoteEvents: List<NostrEvent>,
        repostEvents: List<NostrEvent>,
    ) {
        database.withTransaction {
            val posts = shortTextNoteEvents.mapNotNullAsPost()
            val reposts = repostEvents.mapNotNullAsRepost()
            Timber.i("Received ${posts.size} posts and ${reposts.size} reposts..")

            database.posts().upsertAll(data = posts)
            database.reposts().upsertAll(data = reposts)

            val feedConnections = posts.map { it.postId } + reposts.map { it.postId }
            database.feedsConnections().connect(
                data = feedConnections.map { postId ->
                    FeedPostDataCrossRef(
                        feedDirective = feedDirective,
                        postId = postId
                    )
                }
            )

            database.resources().upsert(data = posts.flatMapAsPostResources())
        }
    }

    private fun List<NostrEvent>.processMetadataEvents() {
        database.profiles().upsertAll(events = mapAsProfileMetadata())
    }

    private fun List<PrimalEvent>.processEventStats() {
        database.eventStats().upsertAll(
            data = this
                .map { NostrJson.decodeFromString<ContentPrimalEventStats>(it.content) }
                .map { it.asEventStatsPO() }
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
