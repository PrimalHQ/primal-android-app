package net.primal.android.highlights.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.toJsonObject
import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseUris
import net.primal.android.highlights.db.HighlightData
import net.primal.android.nostr.ext.getTagValueOrNull
import net.primal.android.nostr.ext.hasReplyMarker
import net.primal.android.nostr.ext.hasRootMarker
import net.primal.android.nostr.ext.isEventIdTag
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.notes.db.PostData

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
        raw = NostrJson.encodeToString(this.toJsonObject()),
        replyToPostId = replyToPostId,
        replyToAuthorId = replyToAuthorId,
    )
}

fun List<JsonArray>.containsRootOrReplyTag() = this.any { it.hasRootMarker() || it.hasReplyMarker() }
