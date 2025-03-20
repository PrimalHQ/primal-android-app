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
import net.primal.android.notes.api.mediator.NoteFeedRemoteMediator
import net.primal.android.notes.db.FeedPost
import net.primal.android.notes.db.sql.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.android.notes.db.sql.ExploreFeedQueryBuilder
import net.primal.android.notes.db.sql.FeedQueryBuilder
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.model.FeedBySpecRequestBody
import net.primal.data.remote.api.feed.model.FeedResponse
import net.primal.data.remote.api.feed.model.ThreadRequestBody

class FeedRepository @Inject constructor(
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
    private val dispatcherProvider: CoroutineDispatcherProvider,
) {

    fun feedBySpec(userId: String, feedSpec: String): Flow<PagingData<FeedPost>> {
        return createPager(userId = userId, feedSpec = feedSpec) {
            database.feedPosts().feedQuery(
                query = feedQueryBuilder(userId = userId, feedSpec = feedSpec).feedQuery(),
            )
        }.flow
    }

    suspend fun findNewestPosts(
        userId: String,
        feedDirective: String,
        limit: Int,
    ) = withContext(dispatcherProvider.io()) {
        database.feedPosts().newestFeedPosts(
            query = feedQueryBuilder(
                userId = userId,
                feedSpec = feedDirective,
            ).newestFeedPostsQuery(limit = limit),
        )
    }

    suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPost> =
        withContext(dispatcherProvider.io()) {
            database.feedPosts().findAllPostsByIds(postIds)
        }

    fun observeConversation(userId: String, noteId: String) =
        database.threadConversations().observeNoteConversation(
            postId = noteId,
            userId = userId,
        )

    suspend fun fetchReplies(userId: String, noteId: String) =
        withContext(dispatcherProvider.io()) {
            val response = feedApi.getThread(ThreadRequestBody(postId = noteId, userPubKey = userId, limit = 100))
            response.persistNoteRepliesAndArticleCommentsToDatabase(noteId = noteId, database = database)
            response.persistToDatabaseAsTransaction(userId = userId, database = database)
        }

    suspend fun removeFeedSpec(userId: String, feedSpec: String) =
        withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys().deleteByDirective(ownerId = userId, directive = feedSpec)
            database.feedsConnections().deleteConnectionsByDirective(ownerId = userId, feedSpec = feedSpec)
            database.articleFeedsConnections().deleteConnectionsBySpec(ownerId = userId, spec = feedSpec)
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
    private fun createPager(
        userId: String,
        feedSpec: String,
        pagingSourceFactory: () -> PagingSource<Int, FeedPost>,
    ) = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE * 3,
            enablePlaceholders = true,
        ),
        remoteMediator = NoteFeedRemoteMediator(
            dispatcherProvider = dispatcherProvider,
            feedSpec = feedSpec,
            userId = userId,
            feedApi = feedApi,
            database = database,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )

    private fun feedQueryBuilder(userId: String, feedSpec: String): FeedQueryBuilder =
        when {
            feedSpec.supportsNoteReposts() -> ChronologicalFeedWithRepostsQueryBuilder(
                feedSpec = feedSpec,
                userPubkey = userId,
            )

            else -> ExploreFeedQueryBuilder(
                feedSpec = feedSpec,
                userPubkey = userId,
            )
        }

    companion object {
        private const val PAGE_SIZE = 25
    }
}
