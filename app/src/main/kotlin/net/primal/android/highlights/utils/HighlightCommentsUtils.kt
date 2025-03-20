package net.primal.android.highlights.utils

import kotlinx.serialization.json.JsonArray
import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseUris
import net.primal.android.highlights.db.HighlightData
import net.primal.android.nostr.ext.getTagValueOrNull
import net.primal.android.nostr.ext.hasReplyMarker
import net.primal.android.nostr.ext.hasRootMarker
import net.primal.android.nostr.ext.isEventIdTag
import net.primal.android.notes.db.PostData
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.serialization.toNostrJsonObject

fun List<NostrEvent>.mapNotNullAsHighlightComments(highlights: List<HighlightData>): List<PostData> =
    this.mapNotNull { it.asHighlightComment(highlights = highlights) }

fun NostrEvent.asHighlightComment(highlights: List<HighlightData>): PostData? {
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
        uris = this.content.parseUris(),
        hashtags = this.parseHashtags(),
        sig = this.sig,
        raw = this.toNostrJsonObject().encodeToJsonString(),
        replyToPostId = replyToPostId,
        replyToAuthorId = replyToAuthorId,
    )
}

fun List<JsonArray>.containsRootOrReplyTag() = this.any { it.hasRootMarker() || it.hasReplyMarker() }
