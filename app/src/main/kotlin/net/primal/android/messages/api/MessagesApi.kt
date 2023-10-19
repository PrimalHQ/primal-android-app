package net.primal.android.messages.api

import net.primal.android.messages.domain.ConversationRelation
import net.primal.android.messages.api.model.ConversationsResponse
import net.primal.android.messages.api.model.MessagesRequestBody
import net.primal.android.messages.api.model.MessagesResponse

interface MessagesApi {

    suspend fun getConversations(userId: String, relation: ConversationRelation): ConversationsResponse

    suspend fun getMessages(body: MessagesRequestBody): MessagesResponse

    suspend fun markMessagesAsRead(userId: String, conversationUserId: String?)

}
