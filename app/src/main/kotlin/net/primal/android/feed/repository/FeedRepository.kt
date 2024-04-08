package net.primal.android.feed.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.isChronologicalFeed
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.mediator.FeedRemoteMediator
import net.primal.android.feed.api.model.ThreadRequestBody
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.db.sql.ChronologicalFeedQueryBuilder
import net.primal.android.feed.db.sql.ExploreFeedQueryBuilder
import net.primal.android.feed.db.sql.FeedQueryBuilder
import net.primal.android.thread.db.ThreadConversationCrossRef
import net.primal.android.user.accounts.active.ActiveAccountStore

class FeedRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
) {
    fun defaultFeed() = database.feeds().first()

    fun observeFeeds() = database.feeds().observeAllFeeds()

    fun observeContainsFeed(directive: String) = database.feeds().observeContainsFeed(directive)

    fun observeFeedByDirective(feedDirective: String) =
        database.feeds().observeFeedByDirective(feedDirective = feedDirective)

    fun feedByDirective(feedDirective: String): Flow<PagingData<FeedPost>> {
        return createPager(feedDirective = feedDirective) {
            database.feedPosts().feedQuery(
                query = feedQueryBuilder(feedDirective = feedDirective).feedQuery(),
            )
        }.flow
    }

    fun findNewestPosts(feedDirective: String, limit: Int) =
        database.feedPosts().newestFeedPosts(
            query = feedQueryBuilder(
                feedDirective = feedDirective,
            ).newestFeedPostsQuery(limit = limit),
        )

    fun observeNewFeedPostsSyncUpdates(feedDirective: String, since: Long) =
        database.feedPostsSync()
            .observeFeedDirective(feedDirective = feedDirective, since = since)
            .filterNotNull()

    fun findPostById(postId: String): FeedPost? = database.feedPosts().findPostById(postId = postId)

    fun observeConversation(postId: String) =
        database.threadConversations().observeConversation(
            postId = postId,
            userId = activeAccountStore.activeUserId(),
        )

    suspend fun fetchReplies(postId: String) {
        val userId = activeAccountStore.activeUserId()
        val response = feedApi.getThread(
            ThreadRequestBody(postId = postId, userPubKey = userId, limit = 100),
        )

        response.persistToDatabaseAsTransaction(userId = userId, database = database)
        database.conversationConnections().connect(
            data = response.posts.map {
                ThreadConversationCrossRef(
                    postId = postId,
                    replyPostId = it.id,
                )
            },
        )
    }

    suspend fun removeFeedDirective(feedDirective: String) {
        withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys().deleteByDirective(feedDirective)
            database.feedsConnections().deleteConnectionsByDirective(feedDirective)
            database.posts().deleteOrphanPosts()
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(feedDirective: String, pagingSourceFactory: () -> PagingSource<Int, FeedPost>) =
        Pager(
            config = PagingConfig(
                pageSize = 25,
                prefetchDistance = 50,
                initialLoadSize = 125,
                enablePlaceholders = true,
            ),
            remoteMediator = FeedRemoteMediator(
                dispatcherProvider = dispatcherProvider,
                feedDirective = feedDirective,
                userId = activeAccountStore.activeUserId(),
                feedApi = feedApi,
                database = database,
            ),
            pagingSourceFactory = pagingSourceFactory,
        )

    private fun feedQueryBuilder(feedDirective: String): FeedQueryBuilder =
        when {
            feedDirective.isChronologicalFeed() -> ChronologicalFeedQueryBuilder(
                feedDirective = feedDirective,
                userPubkey = activeAccountStore.activeUserId(),
            )

            else -> ExploreFeedQueryBuilder(
                feedDirective = feedDirective,
                userPubkey = activeAccountStore.activeUserId(),
            )
        }
}
