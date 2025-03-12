package net.primal.android.messages.chat.model

import java.time.Instant
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.notes.feed.model.NoteNostrUriUi

data class ChatMessageUi(
    val messageId: String,
    val isUserMessage: Boolean,
    val senderId: String,
    val timestamp: Instant,
    val content: String,
    val uris: List<EventUriUi> = emptyList(),
    val nostrUris: List<NoteNostrUriUi> = emptyList(),
    val hashtags: List<String> = emptyList(),
)
