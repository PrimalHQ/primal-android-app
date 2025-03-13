package net.primal.repository.processors.mappers

import net.primal.db.notes.RepostData
import net.primal.networking.model.NostrEvent
import net.primal.repository.findFirstEventId
import net.primal.repository.findFirstProfileId
import net.primal.serialization.json.NostrJson
import net.primal.serialization.json.decodeFromStringOrNull

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
