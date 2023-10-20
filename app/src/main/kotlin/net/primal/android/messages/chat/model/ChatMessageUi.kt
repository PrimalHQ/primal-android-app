package net.primal.android.messages.chat.model

import net.primal.android.core.compose.feed.model.NostrResourceUi
import net.primal.android.core.compose.media.model.MediaResourceUi
import java.time.Instant

data class ChatMessageUi(
    val messageId: String,
    val isUserMessage: Boolean,
    val senderId: String,
    val timestamp: Instant,
    val content: String,
    val mediaResources: List<MediaResourceUi> = emptyList(),
    val nostrResources: List<NostrResourceUi> = emptyList(),
    val hashtags: List<String> = emptyList(),
)
