package net.primal.domain.streams.chat

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.NostrPublishException

interface LiveStreamChatRepository {

    fun observeMessages(streamATag: String): Flow<List<ChatMessage>>

    @Throws(
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
    )
    suspend fun sendMessage(
        userId: String,
        streamATag: String,
        content: String,
    )
}
