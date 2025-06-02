package net.primal.data.repository.feed

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.notes.FeedPost as FeedPostPO
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.local.queries.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.data.local.queries.ExploreFeedQueryBuilder
import net.primal.data.local.queries.FeedQueryBuilder
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.model.FeedBySpecRequestBody
import net.primal.data.remote.api.feed.model.ThreadRequestBody
import net.primal.data.repository.feed.paging.NoteFeedRemoteMediator
import net.primal.data.repository.feed.processors.FeedProcessor
import net.primal.data.repository.feed.processors.persistNoteRepliesAndArticleCommentsToDatabase
import net.primal.data.repository.feed.processors.persistToDatabaseAsTransaction
import net.primal.data.repository.mappers.local.mapAsFeedPostDO
import net.primal.data.repository.mappers.remote.asFeedPageSnapshot
import net.primal.data.repository.utils.performTopologicalSortOrThis
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.feeds.supportsNoteReposts
import net.primal.domain.posts.FeedPageSnapshot
import net.primal.domain.posts.FeedPost as FeedPostDO
import net.primal.domain.posts.FeedRepository
import net.primal.domain.posts.FeedRepository.Companion.DEFAULT_PAGE_SIZE

internal class FeedRepositoryImpl(
    private val feedApi: FeedApi,
    private val database: PrimalDatabase,
    private val dispatcherProvider: DispatcherProvider,
) : FeedRepository {

    override fun feedBySpec(
        userId: String,
        feedSpec: String,
        allowMutedThreads: Boolean,
    ): Flow<PagingData<FeedPostDO>> {
        return createPager(userId = userId, feedSpec = feedSpec) {
            database.feedPosts().feedQuery(
                query = feedQueryBuilder(
                    userId = userId,
                    feedSpec = feedSpec,
                    allowMutedThreads = allowMutedThreads,
                ).feedQuery(),
            )
        }.flow.map { it.map { feedPostPO -> feedPostPO.mapAsFeedPostDO() } }
    }

    override suspend fun findNewestPosts(
        userId: String,
        feedDirective: String,
        allowMutedThreads: Boolean,
        limit: Int,
    ) = withContext(dispatcherProvider.io()) {
        database.feedPosts().newestFeedPosts(
            query = feedQueryBuilder(
                userId = userId,
                feedSpec = feedDirective,
                allowMutedThreads = allowMutedThreads,
            ).newestFeedPostsQuery(limit = limit),
        ).map { it.mapAsFeedPostDO() }
    }

    override suspend fun deletePostById(postId: String, userId: String) =
        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                val post = database.posts().findAndDeletePostById(postId = postId)
                database.feedsConnections().deletePostConnections(eventId = postId)

                post?.replyToPostId?.let { replyToPostId ->
                    database.eventStats()
                        .reduceEventStats(eventId = replyToPostId) {
                            copy(replies = replies - 1)
                        }
                    database.eventUserStats()
                        .reduceEventUserStats(eventId = replyToPostId, userId = userId) {
                            copy(replied = false)
                        }
                }

                database.eventStats().deleteByEventId(eventId = postId)
                database.eventUserStats().deleteByEventId(eventId = postId)
                database.feedPostsRemoteKeys().deleteAllByEventId(eventId = postId)
            }
        }

    override suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPostDO> =
        withContext(dispatcherProvider.io()) {
            database.feedPosts().findAllPostsByIds(postIds).map { it.mapAsFeedPostDO() }
        }

    override suspend fun findPostsById(postId: String): FeedPostDO? =
        withContext(dispatcherProvider.io()) {
            database.feedPosts().findAllPostsByIds(listOf(postId)).firstOrNull()?.mapAsFeedPostDO()
        }

    override suspend fun fetchConversation(
        userId: String,
        noteId: String,
        limit: Int,
    ) {
        withContext(dispatcherProvider.io()) {
            val response = try {
                feedApi.getThread(ThreadRequestBody(postId = noteId, userPubKey = userId, limit = limit))
            } catch (error: NetworkException) {
                throw NetworkException(message = error.message, cause = error)
            }
            response.persistNoteRepliesAndArticleCommentsToDatabase(noteId = noteId, database = database)
            response.persistToDatabaseAsTransaction(userId = userId, database = database)
        }
    }

    override suspend fun fetchReplies(userId: String, noteId: String) =
        withContext(dispatcherProvider.io()) {
            val response = feedApi.getThread(ThreadRequestBody(postId = noteId, userPubKey = userId, limit = 100))
            response.persistNoteRepliesAndArticleCommentsToDatabase(noteId = noteId, database = database)
            response.persistToDatabaseAsTransaction(userId = userId, database = database)
        }

    override suspend fun removeFeedSpec(userId: String, feedSpec: String) =
        withContext(dispatcherProvider.io()) {
            database.feedPostsRemoteKeys().deleteByDirective(ownerId = userId, directive = feedSpec)
            database.feedsConnections().deleteConnectionsByDirective(ownerId = userId, feedSpec = feedSpec)
            database.articleFeedsConnections().deleteConnectionsBySpec(ownerId = userId, spec = feedSpec)
        }

    override suspend fun replaceFeed(
        userId: String,
        feedSpec: String,
        snapshot: FeedPageSnapshot,
    ) = withContext(dispatcherProvider.io()) {
        FeedProcessor(
            feedSpec = feedSpec,
            database = database,
        ).processAndPersistToDatabase(
            userId = userId,
            snapshot = snapshot,
            clearFeed = true,
        )
    }

    override suspend fun fetchFeedPageSnapshot(
        userId: String,
        feedSpec: String,
        notes: String?,
        until: Long?,
        since: Long?,
        order: String?,
        limit: Int,
    ): FeedPageSnapshot =
        withContext(dispatcherProvider.io()) {
            feedApi.getFeedBySpec(
                body = FeedBySpecRequestBody(
                    spec = feedSpec,
                    userPubKey = userId,
                    notes = notes,
                    until = until,
                    since = since,
                    order = order,
                    limit = limit,
                ),
            ).asFeedPageSnapshot()
        }

    override suspend fun findConversation(userId: String, noteId: String): List<FeedPostDO> {
        return observeConversation(userId = userId, noteId = noteId).firstOrNull() ?: emptyList()
    }

    override fun observeConversation(userId: String, noteId: String): Flow<List<FeedPostDO>> {
        return database.threadConversations().observeNoteConversation(
            postId = noteId,
            userId = userId,
        ).map { list ->
            list.map { it.mapAsFeedPostDO() }.performTopologicalSortOrThis()
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(
        userId: String,
        feedSpec: String,
        pagingSourceFactory: () -> PagingSource<Int, FeedPostPO>,
    ) = Pager(
        config = PagingConfig(
            pageSize = DEFAULT_PAGE_SIZE,
            prefetchDistance = DEFAULT_PAGE_SIZE,
            initialLoadSize = DEFAULT_PAGE_SIZE * 3,
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

    private fun feedQueryBuilder(
        userId: String,
        feedSpec: String,
        allowMutedThreads: Boolean,
    ): FeedQueryBuilder =
        when {
            feedSpec.supportsNoteReposts() -> ChronologicalFeedWithRepostsQueryBuilder(
                feedSpec = feedSpec,
                userPubkey = userId,
                allowMutedThreads = allowMutedThreads,
            )

            else -> ExploreFeedQueryBuilder(
                feedSpec = feedSpec,
                userPubkey = userId,
                allowMutedThreads = allowMutedThreads,
            )
        }
}
