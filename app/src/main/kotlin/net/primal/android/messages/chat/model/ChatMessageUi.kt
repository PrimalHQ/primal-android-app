package net.primal.android.messages.chat.model

import java.time.Instant
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.feed.model.NostrUriResourceUi

data class ChatMessageUi(
    val messageId: String,
    val isUserMessage: Boolean,
    val senderId: String,
    val timestamp: Instant,
    val content: String,
    val noteAttachments: List<NoteAttachmentUi> = emptyList(),
    val nostrResources: List<NostrUriResourceUi> = emptyList(),
    val hashtags: List<String> = emptyList(),
)
