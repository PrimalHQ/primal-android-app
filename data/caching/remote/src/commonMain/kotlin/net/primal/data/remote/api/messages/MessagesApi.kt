package net.primal.data.remote.api.messages

import net.primal.data.remote.api.messages.model.ConversationRequestBody
import net.primal.data.remote.api.messages.model.ConversationsResponse
import net.primal.data.remote.api.messages.model.MarkMessagesReadRequestBody
import net.primal.data.remote.api.messages.model.MessagesRequestBody
import net.primal.data.remote.api.messages.model.MessagesResponse
import net.primal.domain.nostr.NostrEvent

interface MessagesApi {

    suspend fun getConversations(body: ConversationRequestBody): ConversationsResponse

    suspend fun getMessages(body: MessagesRequestBody): MessagesResponse

    suspend fun markConversationAsRead(body: MarkMessagesReadRequestBody)

    suspend fun markAllMessagesAsRead(authorization: NostrEvent)
}
