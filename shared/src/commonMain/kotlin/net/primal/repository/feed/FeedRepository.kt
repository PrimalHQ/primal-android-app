package net.primal.repository.feed

import kotlinx.coroutines.flow.Flow
import net.primal.db.notes.FeedPost

interface FeedRepository {
    suspend fun fetchConversation(userId: String, noteId: String)
    suspend fun findConversation(userId: String, noteId: String): List<FeedPost>
    fun observeConversation(userId: String, noteId: String): Flow<List<FeedPost>>
    suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPost>
}
