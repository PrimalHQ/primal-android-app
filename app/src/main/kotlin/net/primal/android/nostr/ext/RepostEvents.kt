package net.primal.android.nostr.ext

import net.primal.android.feed.db.RepostData
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull

fun List<NostrEvent>.mapNotNullAsRepostDataPO() = mapNotNull {
    val repostedPost = NostrJson.decodeFromStringOrNull<NostrEvent>(it.content)

    val repostedPostId = repostedPost?.id ?: it.tags?.findPostId()
    val repostedPostAuthorId = repostedPost?.pubKey ?: it.tags?.findPostAuthorId()

    if (repostedPostId != null && repostedPostAuthorId != null) {
        it.asRepostDataPO(
            postId = repostedPostId,
            postAuthorId = repostedPostAuthorId
        )
    } else null
}

fun NostrEvent.asRepostDataPO(postId: String, postAuthorId: String) = RepostData(
    repostId = this.id,
    authorId = this.pubKey,
    createdAt = this.createdAt,
    tags = this.tags ?: emptyList(),
    sig = this.sig,
    postId = postId,
    postAuthorId = postAuthorId
)
