package net.primal.data.repository.streams

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.repository.mappers.local.asChatMessageDO
import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.streams.chat.ChatMessage
import net.primal.domain.streams.chat.LiveStreamChatRepository

class LiveStreamChatRepositoryImpl(
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
    ) {
//        val unsignedEvent = NostrUnsignedEvent(
//            pubKey = userId,
//            kind = NostrEventKind.LiveChatMessage.value,
//            tags = listOf(
//                streamATag.asReplaceableEventTag(marker = "root"),
//            ),
//            content = content,
//        )
//        primalPublisher.signPublishImportNostrEvent(unsignedEvent)
    }
}
