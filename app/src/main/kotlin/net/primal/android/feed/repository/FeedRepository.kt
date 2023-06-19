package net.primal.android.feed.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.mediator.FeedRemoteMediator
import net.primal.android.feed.api.model.ThreadRequestBody
import net.primal.android.feed.db.ConversationCrossRef
import net.primal.android.feed.db.Feed
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.db.sql.ExploreFeedQueryBuilder
import net.primal.android.feed.db.sql.FeedQueryBuilder
import net.primal.android.feed.db.sql.LatestFeedQueryBuilder
import net.primal.android.feed.feed.isLatestFeed
import net.primal.android.nostr.ext.asEventStatsPO
import net.primal.android.nostr.ext.asPost
import net.primal.android.nostr.ext.asPostResourcePO
import net.primal.android.nostr.ext.mapAsProfileMetadata
import net.primal.android.nostr.ext.takeContentAsNostrEventOrNull
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventResources
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats
import net.primal.android.serialization.NostrJson
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
) {

    fun observeFeeds(): Flow<List<Feed>> = database.feeds().observeAllFeeds()

    suspend fun findFeedByDirective(feedDirective: String) =
        database.feeds().findFeedByDirective(feedDirective = feedDirective)

    fun feedByDirective(feedDirective: String): Flow<PagingData<FeedPost>> {
        return createPager(feedDirective = feedDirective) {
            database.feedPosts().feedQuery(
                query = feedQueryBuilder(feedDirective = feedDirective).feedQuery()
            )
        }.flow
    }

    fun findNewestPosts(feedDirective: String, limit: Int) =
        database.feedPosts().newestFeedPosts(
            query = feedQueryBuilder(feedDirective = feedDirective).newestFeedPostsQuery(limit = limit)
        )

    fun observeNewFeedPostsSyncUpdates(feedDirective: String, since: Long) =
        database.feedPostsSync()
            .observeFeedDirective(feedDirective = feedDirective, since = since)
            .filterNotNull()

    fun findPostById(postId: String): FeedPost = database.feedPosts().findPostById(postId = postId)

    fun observeConversation(postId: String) =
        database.conversations().observeConversation(postId = postId)

    suspend fun fetchReplies(postId: String) = withContext(Dispatchers.IO) {
        val response = feedApi.getThread(
            ThreadRequestBody(
                postId = postId,
                // User hard-coded to Nostr Highlights
                userPubKey = "9a500dccc084a138330a1d1b2be0d5e86394624325d25084d3eca164e7ea698a",
            )
        )

        database.withTransaction {
            response.posts.processShortTextNoteEvents()
            response.referencedPosts.processReferencedEvents()
            response.metadata.processMetadataEvents()
            response.primalEventStats.processEventStats()
            response.primalEventResources.processEventResources()

            database.conversationConnections().connect(
                data = response.posts.map {
                    ConversationCrossRef(
                        postId = postId,
                        replyPostId = it.id,
                    )
                }
            )
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(
        feedDirective: String,
        pagingSourceFactory: () -> PagingSource<Int, FeedPost>,
    ) =
        Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false
            ),
            remoteMediator = FeedRemoteMediator(
                feedDirective = feedDirective,
                feedApi = feedApi,
                database = database,
            ),
            pagingSourceFactory = pagingSourceFactory,
        )

    private fun feedQueryBuilder(feedDirective: String): FeedQueryBuilder = when {
        feedDirective.isLatestFeed() -> LatestFeedQueryBuilder(feedDirective = feedDirective)
        else -> ExploreFeedQueryBuilder(feedDirective = feedDirective)
    }


    private fun List<NostrEvent>.processShortTextNoteEvents() {
        database.posts().upsertAll(data = this.map { it.asPost() })
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
