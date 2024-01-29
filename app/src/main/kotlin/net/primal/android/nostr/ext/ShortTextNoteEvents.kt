package net.primal.android.nostr.ext

import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.toJsonObject
import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseUris
import net.primal.android.feed.db.PostData
import net.primal.android.nostr.model.NostrEvent

fun List<NostrEvent>.mapAsPostDataPO(referencedPosts: List<PostData>) = map { it.asPost(referencedPosts) }

fun NostrEvent.asPost(referencedPosts: List<PostData>): PostData {
    val replyToPostId = this.tags.find { it.hasReplyMarker() }?.getTagValueOrNull()
        ?: this.tags.find { it.hasRootMarker() }?.getTagValueOrNull()
        ?: this.tags.lastOrNull { it.isEventIdTag() }?.getTagValueOrNull()

    val replyToAuthorId = referencedPosts.find { it.postId == replyToPostId }?.authorId

    return PostData(
        postId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        content = this.content,
        uris = this.content.parseUris(),
        hashtags = this.parseHashtags(),
        sig = this.sig,
        raw = NostrJson.encodeToString(this.toJsonObject()),
        replyToPostId = replyToPostId,
        replyToAuthorId = replyToAuthorId,
    )
}
