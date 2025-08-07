package net.primal.android.stream.ui

import net.primal.android.events.ui.EventZapUiModel

sealed interface StreamChatItem {
    val timestamp: Long
    val uniqueId: String

    data class ChatMessageItem(
        val message: ChatMessageUi,
    ) : StreamChatItem {
        override val timestamp: Long = message.timestamp
        override val uniqueId: String = message.messageId
    }

    data class ZapMessageItem(
        val zap: EventZapUiModel,
    ) : StreamChatItem {
        override val timestamp: Long = zap.zappedAt
        override val uniqueId: String = zap.id
    }
}
