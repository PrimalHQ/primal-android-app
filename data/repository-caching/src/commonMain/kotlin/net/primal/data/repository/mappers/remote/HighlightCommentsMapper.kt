package net.primal.data.repository.mappers.remote

import kotlinx.serialization.json.JsonArray
import net.primal.core.utils.detectUrls
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.reads.HighlightData
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.getTagValueOrNull
import net.primal.domain.nostr.hasReplyMarker
import net.primal.domain.nostr.hasRootMarker
import net.primal.domain.nostr.isEventIdTag
import net.primal.domain.nostr.serialization.toNostrJsonObject
import net.primal.domain.nostr.utils.parseHashtags
import net.primal.domain.nostr.utils.parseNostrUris

fun List<NostrEvent>.mapNotNullAsHighlightComments(highlights: List<HighlightData>): List<PostData> =
    this.mapNotNull { it.asHighlightComment(highlights = highlights) }

private fun NostrEvent.asHighlightComment(highlights: List<HighlightData>): PostData? {
    if (!this.tags.containsRootOrReplyTag()) {
        return null
    }

    val replyToPostId = this.tags.find { it.isEventIdTag() }?.getTagValueOrNull()

    val replyToAuthorId = highlights.find { it.highlightId == replyToPostId }?.authorId

    return PostData(
        postId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        content = this.content,
        uris = this.content.detectUrls() + this.content.parseNostrUris(),
        hashtags = this.parseHashtags(),
        sig = this.sig,
        raw = this.toNostrJsonObject().encodeToJsonString(),
        replyToPostId = replyToPostId,
        replyToAuthorId = replyToAuthorId,
    )
}

fun List<JsonArray>.containsRootOrReplyTag() = this.any { it.hasRootMarker() || it.hasReplyMarker() }
