package net.primal.data.repository.mappers.remote

import net.primal.core.utils.detectUrls
import net.primal.data.local.dao.messages.DirectMessageData
import net.primal.data.local.encryption.EncryptableString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.utils.parseHashtags
import net.primal.domain.nostr.utils.parseNostrUris

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
        content = EncryptableString(decrypted = decryptedMessage),
        uris = decryptedMessage.detectUrls() + decryptedMessage.parseNostrUris(),
        hashtags = decryptedMessage.parseHashtags(),
    )
}
