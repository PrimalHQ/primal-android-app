package net.primal.domain.repository

import androidx.paging.PagingData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.error.NetworkException
import net.primal.domain.model.FeedPost

interface FeedRepository {

    fun feedBySpec(userId: String, feedSpec: String): Flow<PagingData<FeedPost>>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchConversation(userId: String, noteId: String)

    suspend fun findConversation(userId: String, noteId: String): List<FeedPost>

    fun observeConversation(userId: String, noteId: String): Flow<List<FeedPost>>

    suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPost>
}
