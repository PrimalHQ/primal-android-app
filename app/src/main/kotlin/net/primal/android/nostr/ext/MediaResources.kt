package net.primal.android.nostr.ext

import net.primal.android.core.utils.detectContentType
import net.primal.android.feed.db.MediaResource
import net.primal.android.feed.db.PostData
import net.primal.android.messages.db.DirectMessageData

fun List<PostData>.flatMapPostsAsMediaResourcePO() = flatMap {
    it.uris.mapAsMediaResourcePO(eventId = it.postId)
}

fun List<DirectMessageData>.flatMapMessagesAsMediaResourcePO() = flatMap {
    it.uris.mapAsMediaResourcePO(eventId = it.messageId)
}

private fun List<String>.mapAsMediaResourcePO(eventId: String) =
    filterNot { it.isNostrUri() }
        .map { url ->
            MediaResource(
                eventId = eventId,
                contentType = url.detectContentType(),
                url = url,
            )
        }
