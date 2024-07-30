package net.primal.android.feed.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.hasReposts
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.mediator.FeedRemoteMediator
import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.feed.api.model.ThreadRequestBody
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.db.sql.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.android.feed.db.sql.ExploreFeedQueryBuilder
import net.primal.android.feed.db.sql.FeedQueryBuilder
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

    suspend fun findNewestPosts(feedDirective: String, limit: Int) =
        withContext(dispatcherProvider.io()) {
            database.feedPosts().newestFeedPosts(
                query = feedQueryBuilder(
                    feedDirective = feedDirective,
                ).newestFeedPostsQuery(limit = limit),
            )
        }

    suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPost> =
        withContext(dispatcherProvider.io()) {
            database.feedPosts().findAllPostsByIds(postIds)
        }

    fun observeConversation(noteId: String) =
        database.threadConversations().observeNoteConversation(
            postId = noteId,
            userId = activeAccountStore.activeUserId(),
        )

    suspend fun fetchReplies(noteId: String) =
        withContext(dispatcherProvider.io()) {
            val userId = activeAccountStore.activeUserId()
            val response = feedApi.getThread(ThreadRequestBody(postId = noteId, userPubKey = userId, limit = 100))
            response.persistNoteRepliesAndArticleCommentsToDatabase(noteId = noteId, database = database)
            response.persistToDatabaseAsTransaction(userId = userId, database = database)
        }

    suspend fun removeFeedDirective(feedDirective: String) =
        withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys().deleteByDirective(feedDirective)
            database.feedsConnections().deleteConnectionsByDirective(feedDirective)
            database.posts().deleteOrphanPosts()
        }

    suspend fun replaceFeedDirective(
        userId: String,
        feedDirective: String,
        response: FeedResponse,
    ) {
        withContext(dispatcherProvider.io()) {
            FeedProcessor(
                feedDirective = feedDirective,
                database = database,
            ).processAndPersistToDatabase(
                userId = userId,
                response = response,
                clearFeed = true,
            )
        }
    }

    suspend fun fetchLatestNotes(
        userId: String,
        feedDirective: String,
        since: Long? = null,
    ) = withContext(dispatcherProvider.io()) {
        feedApi.getFeed(
            body = FeedRequestBody(
                directive = feedDirective,
                userPubKey = userId,
                since = since,
                order = "desc",
                limit = PAGE_SIZE,
            ),
        )
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(feedDirective: String, pagingSourceFactory: () -> PagingSource<Int, FeedPost>) =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE * 2,
                initialLoadSize = PAGE_SIZE * 5,
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
            feedDirective.hasReposts() -> ChronologicalFeedWithRepostsQueryBuilder(
                feedDirective = feedDirective,
                userPubkey = activeAccountStore.activeUserId(),
            )

            else -> ExploreFeedQueryBuilder(
                feedDirective = feedDirective,
                userPubkey = activeAccountStore.activeUserId(),
            )
        }

    companion object {
        private const val PAGE_SIZE = 25
    }
}
