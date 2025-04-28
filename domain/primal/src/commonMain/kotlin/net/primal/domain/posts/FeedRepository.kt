package net.primal.domain.posts

import androidx.paging.PagingData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.common.exception.NetworkException

interface FeedRepository {

    fun feedBySpec(
        userId: String,
        feedSpec: String,
        allowMutedThreads: Boolean = false,
    ): Flow<PagingData<FeedPost>>

    suspend fun findNewestPosts(
        userId: String,
        feedDirective: String,
        allowMutedThreads: Boolean = false,
        limit: Int,
    ): List<FeedPost>

    suspend fun deletePostById(postId: String, userId: String)

    suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPost>

    suspend fun findPostsById(postId: String): FeedPost?

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchReplies(userId: String, noteId: String)

    fun observeConversation(userId: String, noteId: String): Flow<List<FeedPost>>

    suspend fun removeFeedSpec(userId: String, feedSpec: String)

    suspend fun replaceFeed(
        userId: String,
        feedSpec: String,
        snapshot: FeedPageSnapshot,
    )

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchFeedPageSnapshot(
        userId: String,
        feedSpec: String,
        notes: String? = null,
        until: Long? = null,
        since: Long? = null,
        order: String? = "desc",
        limit: Int = DEFAULT_PAGE_SIZE,
    ): FeedPageSnapshot

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchConversation(userId: String, noteId: String)

    suspend fun findConversation(userId: String, noteId: String): List<FeedPost>

    companion object {
        const val DEFAULT_PAGE_SIZE = 25
    }
}
