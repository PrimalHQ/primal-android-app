package net.primal.android.nostr.ext

import kotlinx.serialization.decodeFromString
import net.primal.android.feed.db.RepostData
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.serialization.NostrJson

fun List<NostrEvent>.mapNotNullAsRepost() = mapNotNull {
    val repostedPost = it.content.decodeAsNostrEventOrNull()

    val repostedPostId = repostedPost?.id ?: it.tags.findPostId()
    val repostedPostAuthorId = repostedPost?.pubKey ?: it.tags.findPostAuthorId()

    if (repostedPostId != null && repostedPostAuthorId != null) {
        it.asRepost(
            postId = repostedPostId,
            postAuthorId = repostedPostAuthorId
        )
    } else null
}

fun NostrEvent.asRepost(postId: String, postAuthorId: String) = RepostData(
    repostId = this.id,
    authorId = this.pubKey,
    createdAt = this.createdAt,
    tags = this.tags,
    sig = this.sig,
    postId = postId,
    postAuthorId = postAuthorId
)

fun String.decodeAsNostrEventOrNull(): NostrEvent? {
    return try {
        NostrJson.decodeFromString(this)
    } catch (error: IllegalArgumentException) {
        null
    }
}
