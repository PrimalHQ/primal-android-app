package net.primal.domain.posts

import androidx.paging.PagingData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEventKind

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

    suspend fun findRepostByPostId(postId: String, userId: String): Result<FeedPostRepostInfo>

    suspend fun deleteRepostById(
        postId: String,
        repostId: String,
        userId: String,
    )

    suspend fun deletePostById(postId: String, userId: String)

    suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPost>

    suspend fun findPostsById(postId: String): FeedPost?

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
    suspend fun fetchConversation(
        userId: String,
        noteId: String,
        limit: Int = 100,
        kinds: List<Int> = DEFAULT_THREAD_KINDS,
    )

    suspend fun findConversation(userId: String, noteId: String): List<FeedPost>

    fun observeUserVotedOptions(userId: String, postId: String): Flow<Set<String>>

    companion object {
        const val DEFAULT_PAGE_SIZE = 25

        val DEFAULT_THREAD_KINDS = listOf(
            NostrEventKind.ShortTextNote.value,
            NostrEventKind.PictureNote.value,
            NostrEventKind.Poll.value,
            NostrEventKind.PollResponse.value,
            NostrEventKind.ZapPoll.value,
            NostrEventKind.ZapRequest.value,
            NostrEventKind.Zap.value,
        )
    }
}
