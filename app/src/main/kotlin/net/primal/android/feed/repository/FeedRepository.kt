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
import net.primal.android.core.ext.isLatestFeed
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.mediator.FeedRemoteMediator
import net.primal.android.feed.api.model.ThreadRequestBody
import net.primal.android.feed.db.Feed
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.db.sql.ExploreFeedQueryBuilder
import net.primal.android.feed.db.sql.FeedQueryBuilder
import net.primal.android.feed.db.sql.LatestFeedQueryBuilder
import net.primal.android.nostr.ext.asEventStatsPO
import net.primal.android.nostr.ext.asEventUserStatsPO
import net.primal.android.nostr.ext.asMediaResourcePO
import net.primal.android.nostr.ext.asPost
import net.primal.android.nostr.ext.mapAsProfileMetadata
import net.primal.android.nostr.ext.takeContentAsNostrEventOrNull
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventResources
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats
import net.primal.android.nostr.model.primal.content.ContentPrimalEventUserStats
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull
import net.primal.android.thread.db.ConversationCrossRef
import net.primal.android.user.active.ActiveAccountStore
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
) {

    fun observeFeeds() = database.feeds().observeAllFeeds()

    fun observeContainsFeed(directive: String) = database.feeds().observeContainsFeed(directive)

    suspend fun addToUserFeeds(title: String, directive: String) {
        val newFeed = Feed(name = title, directive = directive)
        withContext(Dispatchers.IO) {
            database.feeds().upsertAll(listOf(newFeed))
        }
    }

    suspend fun removeFromUserFeeds(directive: String) {
        withContext(Dispatchers.IO) {
            database.feeds().delete(directive = directive)
        }
    }

    fun observeFeedByDirective(feedDirective: String) =
        database.feeds().observeFeedByDirective(feedDirective = feedDirective)

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

    fun findPostById(postId: String): FeedPost? = database.feedPosts().findPostById(postId = postId)

    fun observeConversation(postId: String) =
        database.conversations().observeConversation(
            postId = postId,
            userId = activeAccountStore.activeUserId()
        )

    suspend fun fetchReplies(postId: String) = withContext(Dispatchers.IO) {
        val response = feedApi.getThread(
            ThreadRequestBody(postId = postId, userPubKey = activeAccountStore.activeUserId())
        )

        database.withTransaction {
            response.posts.processShortTextNoteEvents()
            response.referencedPosts.processReferencedEvents()
            response.metadata.processMetadataEvents()
            response.primalEventStats.processEventStats()
            response.primalEventUserStats.processEventUserStats()
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
                pageSize = 60,
                enablePlaceholders = false
            ),
            remoteMediator = FeedRemoteMediator(
                feedDirective = feedDirective,
                userPubkey = activeAccountStore.activeUserId(),
                feedApi = feedApi,
                database = database,
            ),
            pagingSourceFactory = pagingSourceFactory,
        )

    private fun feedQueryBuilder(feedDirective: String): FeedQueryBuilder = when {
        feedDirective.isLatestFeed() -> LatestFeedQueryBuilder(
            feedDirective = feedDirective,
            userPubkey = activeAccountStore.activeUserId()
        )
        else -> ExploreFeedQueryBuilder(
            feedDirective = feedDirective,
            userPubkey = activeAccountStore.activeUserId()
        )
    }


    private fun List<NostrEvent>.processShortTextNoteEvents() {
        database.posts().upsertAll(data = this.map { it.asPost() })
    }

    private fun List<NostrEvent>.processMetadataEvents() {
        database.profiles().upsertAll(profiles = mapAsProfileMetadata())
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
                .mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventStats>(it.content) }
                .map { it.asEventStatsPO() }
        )
    }

    private fun List<PrimalEvent>.processEventUserStats() {
        database.postUserStats().upsertAll(
            data = this
                .mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventUserStats>(it.content) }
                .map { it.asEventUserStatsPO(userId = activeAccountStore.activeUserId()) }
        )
    }

    private fun List<PrimalEvent>.processEventResources() {
        database.resources().upsert(
            data = this
                .mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventResources>(it.content) }
                .flatMap {
                    val eventId = it.eventId
                    it.resources.map { eventResource ->
                        eventResource.asMediaResourcePO(eventId = eventId)
                    }
                }
        )
    }

}
