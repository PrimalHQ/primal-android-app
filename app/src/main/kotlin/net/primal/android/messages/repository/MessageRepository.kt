package net.primal.android.messages.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.bechToBytesOrThrow
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.db.PrimalDatabase
import net.primal.android.messages.api.MessagesApi
import net.primal.android.messages.api.mediator.MessagesProcessor
import net.primal.android.messages.api.mediator.MessagesRemoteMediator
import net.primal.android.messages.api.model.MessagesRequestBody
import net.primal.android.messages.db.DirectMessage
import net.primal.android.messages.db.MessageConversation
import net.primal.android.messages.db.MessageConversationData
import net.primal.android.messages.domain.ConversationRelation
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore

@OptIn(ExperimentalPagingApi::class)
class MessageRepository @Inject constructor(
    private val credentialsStore: CredentialsStore,
    private val database: PrimalDatabase,
    private val messagesApi: MessagesApi,
    private val messagesProcessor: MessagesProcessor,
    private val nostrPublisher: NostrPublisher,
) {

    fun newestConversations(userId: String, relation: ConversationRelation) =
        createConversationsPager {
            database.messageConversations().newestConversationsPagedByOwnerId(ownerId = userId, relation = relation)
        }.flow

    fun newestMessages(userId: String, participantId: String) =
        createMessagesPager(userId = userId, participantId = participantId) {
            database.messages().newestMessagesPagedByOwnerId(ownerId = userId, participantId = participantId)
        }.flow

    private suspend fun fetchConversations(userId: String, relation: ConversationRelation) {
        val response = withContext(Dispatchers.IO) {
            messagesApi.getConversations(
                userId = userId,
                relation = relation,
            )
        }

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
                    ownerId = userId,
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
                messagesProcessor.processMessageEventsAndSave(
                    userId = userId,
                    messages = response.messages,
                    profileMetadata = response.profileMetadata,
                    mediaResources = response.cdnResources,
                    primalUserNames = response.primalUserNames,
                    primalPremiumInfo = response.primalPremiumInfo,
                    primalLegendProfiles = response.primalLegendProfiles,
                    blossomServerEvents = response.blossomServers,
                )
                database.messageConversations().upsertAll(data = messageConversation)
            }
        }
    }

    suspend fun fetchFollowConversations(userId: String) =
        fetchConversations(
            userId = userId,
            relation = ConversationRelation.Follows,
        )

    suspend fun fetchNonFollowsConversations(userId: String) =
        fetchConversations(
            userId = userId,
            relation = ConversationRelation.Other,
        )

    suspend fun fetchNewConversationMessages(userId: String, conversationUserId: String) {
        withContext(Dispatchers.IO) {
            val latestMessage = database.messages().firstByOwnerId(ownerId = userId, participantId = conversationUserId)
            val response = messagesApi.getMessages(
                body = MessagesRequestBody(
                    userId = userId,
                    participantId = conversationUserId,
                    since = latestMessage?.createdAt ?: 0,
                ),
            )
            messagesProcessor.processMessageEventsAndSave(
                userId = userId,
                messages = response.messages,
                profileMetadata = response.profileMetadata,
                mediaResources = response.cdnResources,
                primalUserNames = response.primalUserNames,
                primalPremiumInfo = response.primalPremiumInfo,
                primalLegendProfiles = response.primalLegendProfiles,
                blossomServerEvents = response.blossomServers,
            )
        }
    }

    suspend fun markConversationAsRead(userId: String, conversationUserId: String) {
        withContext(Dispatchers.IO) {
            messagesApi.markConversationAsRead(
                userId = userId,
                conversationUserId = conversationUserId,
            )
            database.messageConversations().markConversationAsRead(
                ownerId = userId,
                participantId = conversationUserId,
            )
        }
    }

    suspend fun markAllMessagesAsRead(userId: String) {
        withContext(Dispatchers.IO) {
            messagesApi.markAllMessagesAsRead(userId = userId)
            database.messageConversations().markAllConversationAsRead(ownerId = userId)
        }
    }

    suspend fun sendMessage(
        userId: String,
        receiverId: String,
        text: String,
    ) {
        val encryptedContent = CryptoUtils.encrypt(
            msg = text,
            privateKey = credentialsStore
                .findOrThrow(npub = userId.hexToNpubHrp())
                .nsec.bechToBytesOrThrow(hrp = "nsec"),
            pubKey = receiverId.hexToNpubHrp().bechToBytesOrThrow(hrp = "npub"),
        )

        withContext(Dispatchers.IO) {
            val nostrEvent = nostrPublisher.publishDirectMessage(
                userId = userId,
                receiverId = receiverId,
                encryptedContent = encryptedContent,
            )
            messagesProcessor.processMessageEventsAndSave(
                userId = userId,
                messages = listOf(nostrEvent),
                profileMetadata = emptyList(),
                mediaResources = emptyList(),
                primalUserNames = null,
                primalPremiumInfo = null,
                primalLegendProfiles = null,
                blossomServerEvents = emptyList(),
            )
        }
    }

    private fun createConversationsPager(pagingSourceFactory: () -> PagingSource<Int, MessageConversation>) =
        Pager(
            config = PagingConfig(
                pageSize = 50,
                prefetchDistance = 100,
                initialLoadSize = 200,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = pagingSourceFactory,
        )

    private fun createMessagesPager(
        userId: String,
        participantId: String,
        pagingSourceFactory: () -> PagingSource<Int, DirectMessage>,
    ) = Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 100,
            initialLoadSize = 200,
            enablePlaceholders = true,
        ),
        remoteMediator = MessagesRemoteMediator(
            userId = userId,
            participantId = participantId,
            database = database,
            messagesApi = messagesApi,
            messagesProcessor = messagesProcessor,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )
}
