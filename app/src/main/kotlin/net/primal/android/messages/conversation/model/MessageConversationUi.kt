package net.primal.android.messages.conversation.model

import net.primal.android.core.compose.media.model.MediaResourceUi
import java.time.Instant

data class MessageConversationUi(
    val participantId: String,
    val participantUsername: String,
    val lastMessageSnippet: String,
    val lastMessageAt: Instant,
    val participantInternetIdentifier: String? = null,
    val participantAvatarUrl: String? = null,
    val participantMediaResources: List<MediaResourceUi> = emptyList(),
    val unreadMessagesCount: Int = 0,
)
