package net.primal.android.notes.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.feeds.domain.supportsNoteReposts
import net.primal.android.notes.api.FeedApi
import net.primal.android.notes.api.mediator.FeedRemoteMediator
import net.primal.android.notes.api.model.FeedBySpecRequestBody
import net.primal.android.notes.api.model.FeedResponse
import net.primal.android.notes.api.model.ThreadRequestBody
import net.primal.android.notes.db.FeedPost
import net.primal.android.notes.db.sql.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.android.notes.db.sql.ExploreFeedQueryBuilder
import net.primal.android.notes.db.sql.FeedQueryBuilder
import net.primal.android.user.accounts.active.ActiveAccountStore

class FeedRepository @Inject constructor(
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
    private val dispatcherProvider: CoroutineDispatcherProvider,
) {
    fun observeContainsFeed(feedSpec: String) = database.feeds().observeContainsFeed(feedSpec)

    fun feedBySpec(feedSpec: String): Flow<PagingData<FeedPost>> {
        return createPager(feedSpec = feedSpec) {
            database.feedPosts().feedQuery(
                query = feedQueryBuilder(feedSpec = feedSpec).feedQuery(),
            )
        }.flow
    }

    suspend fun findNewestPosts(feedDirective: String, limit: Int) =
        withContext(dispatcherProvider.io()) {
            database.feedPosts().newestFeedPosts(
                query = feedQueryBuilder(
                    feedSpec = feedDirective,
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

    suspend fun removeFeedSpec(feedSpec: String) =
        withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys().deleteByDirective(feedSpec)
            database.feedsConnections().deleteConnectionsByDirective(feedSpec)
            database.posts().deleteOrphanPosts()
        }

    suspend fun replaceFeedSpec(
        userId: String,
        feedSpec: String,
        response: FeedResponse,
    ) = withContext(dispatcherProvider.io()) {
        FeedProcessor(
            feedSpec = feedSpec,
            database = database,
        ).processAndPersistToDatabase(
            userId = userId,
            response = response,
            clearFeed = true,
        )
    }

    suspend fun fetchLatestNotes(
        userId: String,
        feedSpec: String,
        since: Long? = null,
    ) = withContext(dispatcherProvider.io()) {
        feedApi.getFeedBySpec(
            body = FeedBySpecRequestBody(
                spec = feedSpec,
                userPubKey = userId,
                since = since,
                order = "desc",
                limit = PAGE_SIZE,
            ),
        )
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(feedSpec: String, pagingSourceFactory: () -> PagingSource<Int, FeedPost>) =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE * 2,
                initialLoadSize = PAGE_SIZE * 5,
                enablePlaceholders = true,
            ),
            remoteMediator = FeedRemoteMediator(
                dispatcherProvider = dispatcherProvider,
                feedSpec = feedSpec,
                userId = activeAccountStore.activeUserId(),
                feedApi = feedApi,
                database = database,
            ),
            pagingSourceFactory = pagingSourceFactory,
        )

    private fun feedQueryBuilder(feedSpec: String): FeedQueryBuilder =
        when {
            feedSpec.supportsNoteReposts() -> ChronologicalFeedWithRepostsQueryBuilder(
                feedSpec = feedSpec,
                userPubkey = activeAccountStore.activeUserId(),
            )

            else -> ExploreFeedQueryBuilder(
                feedSpec = feedSpec,
                userPubkey = activeAccountStore.activeUserId(),
            )
        }

    companion object {
        private const val PAGE_SIZE = 25
    }
}
