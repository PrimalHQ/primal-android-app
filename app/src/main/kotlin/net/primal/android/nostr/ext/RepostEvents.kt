package net.primal.android.nostr.ext

import net.primal.android.feed.db.RepostData
import net.primal.android.nostr.model.NostrEvent

fun List<NostrEvent>.mapNotNullAsRepost() = mapNotNull {
    val repostedPostId = it.tags.findPostId()
    val repostedPostAuthorId = it.tags.findPostAuthorId()
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