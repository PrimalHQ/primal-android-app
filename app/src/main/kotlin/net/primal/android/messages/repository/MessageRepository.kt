package net.primal.android.messages.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.db.PrimalDatabase
import net.primal.android.messages.api.MessagesApi
import net.primal.android.messages.api.mediator.MessagesRemoteMediator
import net.primal.android.messages.api.mediator.processAndSave
import net.primal.android.messages.api.model.MessagesRequestBody
import net.primal.android.messages.db.DirectMessage
import net.primal.android.messages.db.MessageConversation
import net.primal.android.messages.db.MessageConversationData
import net.primal.android.messages.domain.ConversationRelation
import net.primal.android.nostr.ext.flatMapNotNullAsMediaResourcePO
import net.primal.android.nostr.ext.mapAsMessageDataPO
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class MessageRepository @Inject constructor(
    private val messagesApi: MessagesApi,
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
    private val credentialsStore: CredentialsStore,
) {

    fun newestConversations(relation: ConversationRelation) = createConversationsPager {
        database.messageConversations().newestConversationsPaged(relation = relation)
    }.flow

    fun newestMessages(participantId: String) = createMessagesPager(participantId = participantId) {
        database.messages().newestMessagesPaged(participantId = participantId)
    }.flow

    private suspend fun fetchConversations(relation: ConversationRelation) {
        val userId = activeAccountStore.activeUserId()
        val response = withContext(Dispatchers.IO) {
            messagesApi.getConversations(
                userId = userId,
                relation = relation,
            )
        }

        val profiles = response.profileMetadata.mapAsProfileDataPO()
        val primalMediaResources = response.mediaResources.flatMapNotNullAsMediaResourcePO()
        val messages = response.messages.mapAsMessageDataPO(
            userId = userId,
            nsec = credentialsStore.findOrThrow(npub = userId.hexToNpubHrp()).nsec
        )

        val summary = response.conversationsSummary
        val rawConversations = summary?.summaryPerParticipantId?.keys ?: emptyList()
        val messageConversation = rawConversations
            .mapNotNull { participantId ->
                summary?.summaryPerParticipantId?.get(participantId)?.let { summary ->
                    participantId to summary
                }
            }
            .map { (participantId, summary) ->
                MessageConversationData(
                    participantId = participantId,
                    participantMetadataId = response.profileMetadata
                        .find { it.pubKey == participantId }
                        ?.id,
                    lastMessageId = summary.lastMessageId,
                    lastMessageAt = summary.lastMessageAt,
                    unreadMessagesCount = summary.count,
                    relation = relation,
                )
            }

        withContext(Dispatchers.IO) {
            database.withTransaction {
                database.profiles().upsertAll(data = profiles)
                database.mediaResources().upsertAll(data = primalMediaResources)
                database.messages().upsertAll(data = messages)
                database.messageConversations().upsertAll(data = messageConversation)
            }
        }
    }

    suspend fun fetchFollowConversations() =
        fetchConversations(relation = ConversationRelation.Follows)

    suspend fun fetchNonFollowsConversations() =
        fetchConversations(relation = ConversationRelation.Other)

    suspend fun fetchNewConversationMessages(userId: String, conversationUserId: String) {
        val response = withContext(Dispatchers.IO) {
            val latestMessage = database.messages().first(participantId = conversationUserId)
            messagesApi.getMessages(
                body = MessagesRequestBody(
                    userId = userId,
                    participantId = conversationUserId,
                    since = latestMessage?.createdAt ?: 0,
                )
            )
        }

        response.processAndSave(
            userId = userId,
            database = database,
            credentialsStore = credentialsStore,
        )
    }

    suspend fun markConversationAsRead(userId: String, conversationUserId: String) {
        withContext(Dispatchers.IO) {
            messagesApi.markConversationAsRead(
                userId = userId,
                conversationUserId = conversationUserId
            )
            database.messageConversations().markConversationAsRead(
                participantId = conversationUserId
            )
        }
    }

    suspend fun markAllMessagesAsRead(userId: String) {
        withContext(Dispatchers.IO) {
            messagesApi.markAllMessagesAsRead(userId = userId)
            database.messageConversations().markAllConversationAsRead()
        }
    }

    private fun createConversationsPager(
        pagingSourceFactory: () -> PagingSource<Int, MessageConversation>
    ) = Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 100,
            initialLoadSize = 200,
            enablePlaceholders = true,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )

    private fun createMessagesPager(
        participantId: String,
        pagingSourceFactory: () -> PagingSource<Int, DirectMessage>
    ) = Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 100,
            initialLoadSize = 200,
            enablePlaceholders = true,
        ),
        remoteMediator = MessagesRemoteMediator(
            userId = activeAccountStore.activeUserId(),
            participantId = participantId,
            messagesApi = messagesApi,
            database = database,
            credentialsStore = credentialsStore,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )

}
