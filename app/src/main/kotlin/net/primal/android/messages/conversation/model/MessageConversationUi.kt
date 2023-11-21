package net.primal.android.messages.conversation.model

import java.time.Instant
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.feed.model.NoteNostrUriUi

data class MessageConversationUi(
    val participantId: String,
    val participantUsername: String,
    val lastMessageId: String,
    val lastMessageSnippet: String,
    val lastMessageAttachments: List<NoteAttachmentUi>,
    val lastMessageNostrUris: List<NoteNostrUriUi>,
    val lastMessageAt: Instant,
    val isLastMessageFromUser: Boolean,
    val participantInternetIdentifier: String? = null,
    val participantAvatarCdnImage: CdnImage? = null,
    val unreadMessagesCount: Int = 0,
)
