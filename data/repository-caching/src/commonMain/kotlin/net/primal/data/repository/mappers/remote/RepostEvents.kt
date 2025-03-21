package net.primal.data.repository.mappers.remote

import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.local.dao.notes.RepostData
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstEventId
import net.primal.domain.nostr.findFirstProfileId

fun List<NostrEvent>.mapNotNullAsRepostDataPO() =
    mapNotNull {
        val repostedPost = it.content.decodeFromJsonStringOrNull<NostrEvent>()

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
