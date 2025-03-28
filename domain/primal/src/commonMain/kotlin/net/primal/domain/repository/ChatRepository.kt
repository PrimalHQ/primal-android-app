package net.primal.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.domain.ConversationRelation
import net.primal.domain.model.DMConversation
import net.primal.domain.model.DirectMessage
import net.primal.domain.nostr.NostrEvent

interface ChatRepository {

    fun newestConversations(userId: String, relation: ConversationRelation): Flow<PagingData<DMConversation>>

    fun newestMessages(userId: String, participantId: String): Flow<PagingData<DirectMessage>>

    suspend fun fetchFollowConversations(userId: String)

    suspend fun fetchNonFollowsConversations(userId: String)

    suspend fun fetchNewConversationMessages(userId: String, conversationUserId: String)

    suspend fun markConversationAsRead(authorization: NostrEvent, conversationUserId: String)

    suspend fun markAllMessagesAsRead(authorization: NostrEvent)

    suspend fun sendMessage(
        userId: String,
        receiverId: String,
        text: String,
    )
}
