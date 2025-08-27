package net.primal.data.repository.streams

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.local.dao.streams.StreamChatMessageData
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.repository.mappers.local.asChatMessageDO
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asReplaceableEventTag
import net.primal.domain.nostr.findFirstClient
import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.streams.chat.ChatMessage
import net.primal.domain.streams.chat.LiveStreamChatRepository

class LiveStreamChatRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
    private val primalPublisher: PrimalPublisher,
) : LiveStreamChatRepository {

    override fun observeMessages(streamATag: String): Flow<List<ChatMessage>> {
        return database.streamChats().observeMessages(streamATag = streamATag)
            .map { list ->
                list.mapNotNull { it.asChatMessageDO() }
            }
    }

    override suspend fun sendMessage(
        userId: String,
        streamATag: String,
        content: String,
    ) = withContext(dispatcherProvider.io()) {
        val unsignedEvent = NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.ChatMessage.value,
            tags = listOf(
                streamATag.asReplaceableEventTag(marker = "root"),
            ),
            content = content,
        )
        val publishedMessage = primalPublisher.signPublishImportNostrEvent(unsignedEvent)

        database.streamChats().upsert(
            StreamChatMessageData(
                messageId = publishedMessage.nostrEvent.id,
                streamATag = streamATag,
                authorId = userId,
                createdAt = publishedMessage.nostrEvent.createdAt,
                content = content,
                raw = publishedMessage.nostrEvent.encodeToJsonString(),
                client = publishedMessage.nostrEvent.tags.findFirstClient(),
            ),
        )
    }

    override suspend fun clearMessages(streamATag: String) =
        withContext(dispatcherProvider.io()) {
            database.streamChats().deleteMessages(streamATag)
        }
}
