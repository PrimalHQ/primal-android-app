package net.primal.domain.repository

//import net.primal.db.notes.FeedPost

// TODO Fix dependency on db entity
interface FeedRepository {
    suspend fun fetchConversation(userId: String, noteId: String)
//    suspend fun findConversation(userId: String, noteId: String): List<FeedPost>
//    fun observeConversation(userId: String, noteId: String): Flow<List<FeedPost>>
//    suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPost>
}
