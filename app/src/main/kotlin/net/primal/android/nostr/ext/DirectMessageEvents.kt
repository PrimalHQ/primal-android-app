package net.primal.android.nostr.ext

import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseUris
import net.primal.android.messages.db.DirectMessageData
import net.primal.android.messages.domain.MessagesUnreadCount
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

fun PrimalEvent.asMessagesTotalCount(): MessagesUnreadCount? {
    return this.content.toIntOrNull()?.let {
        MessagesUnreadCount(count = it)
    }
}

fun List<NostrEvent>.mapAsMessageDataPO(
    userId: String,
    onMessageDecrypt: (userId: String, participantId: String, content: String) -> String,
) = mapNotNull { it.mapAsMessageDataPO(userId = userId, onMessageDecrypt = onMessageDecrypt) }

fun NostrEvent.mapAsMessageDataPO(
    userId: String,
    onMessageDecrypt: (userId: String, participantId: String, content: String) -> String,
): DirectMessageData? {
    val senderId = this.pubKey
    val receiverId = this.tags.findFirstProfileId() ?: return null
    val participantId = if (senderId != userId) senderId else receiverId

    val decryptedMessage = onMessageDecrypt(userId, participantId, this.content)

    return DirectMessageData(
        ownerId = userId,
        messageId = this.id,
        senderId = senderId,
        receiverId = receiverId,
        participantId = participantId,
        createdAt = this.createdAt,
        content = decryptedMessage,
        uris = decryptedMessage.parseUris(),
        hashtags = decryptedMessage.parseHashtags(),
    )
}
