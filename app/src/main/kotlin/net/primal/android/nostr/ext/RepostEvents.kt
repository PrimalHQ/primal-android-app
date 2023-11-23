package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.feed.db.RepostData
import net.primal.android.nostr.model.NostrEvent

fun List<NostrEvent>.mapNotNullAsRepostDataPO() =
    mapNotNull {
        val repostedPost = NostrJson.decodeFromStringOrNull<NostrEvent>(it.content)

        val repostedPostId = repostedPost?.id ?: it.tags.findFirstEventId()
        val repostedPostAuthorId = repostedPost?.pubKey ?: it.tags.findFirstProfileId()

        if (repostedPostId != null && repostedPostAuthorId != null) {
            it.asRepostDataPO(
                postId = repostedPostId,
                postAuthorId = repostedPostAuthorId,
            )
        } else {
            null
        }
    }

fun NostrEvent.asRepostDataPO(postId: String, postAuthorId: String) =
    RepostData(
        repostId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        sig = this.sig,
        postId = postId,
        postAuthorId = postAuthorId,
    )
