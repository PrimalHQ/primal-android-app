package net.primal.domain.repository

import androidx.paging.PagingData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.ConversationRelation
import net.primal.domain.error.NetworkException
import net.primal.domain.model.DMConversation
import net.primal.domain.model.DirectMessage
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.MessageEncryptException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.NostrPublishException

interface ChatRepository {

    fun newestConversations(userId: String, relation: ConversationRelation): Flow<PagingData<DMConversation>>

    fun newestMessages(userId: String, participantId: String): Flow<PagingData<DirectMessage>>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchFollowConversations(userId: String)

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchNonFollowsConversations(userId: String)

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchNewConversationMessages(userId: String, conversationUserId: String)

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun markConversationAsRead(authorization: NostrEvent, conversationUserId: String)

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun markAllMessagesAsRead(authorization: NostrEvent)

    @Throws(
        MessageEncryptException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
    )
    suspend fun sendMessage(
        userId: String,
        receiverId: String,
        text: String,
    )
}
