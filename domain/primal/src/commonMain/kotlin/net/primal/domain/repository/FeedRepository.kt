package net.primal.domain.repository

import androidx.paging.PagingData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.error.NetworkException
import net.primal.domain.model.FeedPost

interface FeedRepository {

    fun feedBySpec(userId: String, feedSpec: String): Flow<PagingData<FeedPost>>

    suspend fun findNewestPosts(
        userId: String,
        feedDirective: String,
        limit: Int,
    ): List<FeedPost>

    suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPost>

    suspend fun findPostsById(postId: String): FeedPost?

    suspend fun fetchReplies(userId: String, noteId: String)

    fun observeConversation(userId: String, noteId: String): Flow<List<FeedPost>>

    suspend fun removeFeedSpec(userId: String, feedSpec: String)

    // TODO Port replaceFeedSpec (New Posts Pill)
//    suspend fun replaceFeedSpec(
//        userId: String,
//        feedSpec: String,
//        response: FeedResponse,
//    ) = withContext(dispatcherProvider.io()) {
//        FeedProcessor(
//            feedSpec = feedSpec,
//            database = database,
//        ).processAndPersistToDatabase(
//            userId = userId,
//            response = response,
//            clearFeed = true,
//        )
//    }
//
    // TODO Port fetchLatestNotes (New Posts Pill)
//    suspend fun fetchLatestNotes(
//        userId: String,
//        feedSpec: String,
//        since: Long? = null,
//    ) = withContext(dispatcherProvider.io()) {
//        feedApi.getFeedBySpec(
//            body = FeedBySpecRequestBody(
//                spec = feedSpec,
//                userPubKey = userId,
//                since = since,
//                order = "desc",
//                limit = PAGE_SIZE,
//            ),
//        )
//    }

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchConversation(userId: String, noteId: String)

    suspend fun findConversation(userId: String, noteId: String): List<FeedPost>
}
