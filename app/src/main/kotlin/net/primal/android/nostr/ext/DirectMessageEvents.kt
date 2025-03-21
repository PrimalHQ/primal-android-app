package net.primal.android.nostr.ext

import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseUris
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.bechToBytesOrThrow
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.messages.db.DirectMessageData
import net.primal.android.messages.domain.MessagesUnreadCount
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.serialization.toNostrJsonObject
import timber.log.Timber

fun PrimalEvent.asMessagesTotalCount(): MessagesUnreadCount? {
    return this.content.toIntOrNull()?.let {
        MessagesUnreadCount(count = it)
    }
}

fun List<NostrEvent>.mapAsMessageDataPO(userId: String, nsec: String?) =
    mapNotNull { it.mapAsMessageDataPO(userId = userId, nsec = nsec) }

fun NostrEvent.mapAsMessageDataPO(userId: String, nsec: String?): DirectMessageData? {
    val senderId = this.pubKey
    val receiverId = this.tags.findFirstProfileId() ?: return null
    val participantId = if (senderId != userId) senderId else receiverId

    val decryptedMessage = runCatching {
        CryptoUtils.decrypt(
            message = this.content,
            privateKey = nsec?.bechToBytesOrThrow(hrp = "nsec") ?: throw MissingPrivateKeyException(),
            pubKey = participantId.hexToNpubHrp().bechToBytesOrThrow(hrp = "npub"),
        )
    }.getOrElse {
        Timber.w(this.toNostrJsonObject().encodeToJsonString())
        this.content
    }

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
