package net.primal.android.nostr.ext

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.toJsonObject
import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseUris
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.bechToBytes
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.messages.db.DirectMessageData
import net.primal.android.messages.domain.ConversationSummary
import net.primal.android.messages.domain.ConversationsSummary
import net.primal.android.messages.domain.MessagesUnreadCount
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import timber.log.Timber

fun PrimalEvent.asMessagesTotalCount(): MessagesUnreadCount? {
    return this.content.toIntOrNull()?.let {
        MessagesUnreadCount(count = it)
    }
}

fun PrimalEvent.asMessageConversationsSummary(): ConversationsSummary {
    val jsonObject = NostrJson.parseToJsonElement(this.content).jsonObject
    val map = mutableMapOf<String, ConversationSummary>()
    jsonObject.keys.forEach {
        jsonObject[it]?.jsonObject?.let { summaryJson ->
            val summary = NostrJson.decodeFromJsonElement<ConversationSummary>(summaryJson)
            map[it] = summary
        }
    }
    return ConversationsSummary(summaryPerParticipantId = map)
}

fun List<NostrEvent>.mapAsMessageDataPO(userId: String, nsec: String) =
    mapNotNull { it.mapAsMessageDataPO(userId = userId, nsec = nsec) }

fun NostrEvent.mapAsMessageDataPO(userId: String, nsec: String): DirectMessageData? {
    val senderId = this.pubKey
    val receiverId = this.tags.findFirstProfileId() ?: return null
    val participantId = if (senderId != userId) senderId else receiverId

    val decryptedMessage = runCatching {
        CryptoUtils.decrypt(
            message = this.content,
            privateKey = nsec.bechToBytes(hrp = "nsec"),
            pubKey = participantId.hexToNpubHrp().bechToBytes(hrp = "npub"),
        )
    }.getOrElse {
        Timber.w(NostrJson.encodeToString(this.toJsonObject()))
        this.content
    }

    return DirectMessageData(
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
