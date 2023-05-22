package net.primal.android.nostr.processor

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.RepostData
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind

class RepostEventProcessor(
    private val database: PrimalDatabase,
) : NostrEventProcessor {

    override val kind = NostrEventKind.Reposts

    override fun process(events: List<NostrEvent>) {
        database.reposts().upsertAll(
            data = events.mapNotNull {
                val referencedPostId = it.tags.findPostId()
                val referencedPostAuthorId = it.tags.findPostAuthorId()
                if (referencedPostId != null && referencedPostAuthorId != null) {
                    it.asRepost(
                        postId = referencedPostId,
                        postAuthorId = referencedPostAuthorId
                    )
                } else null
            }
        )
    }

    private fun NostrEvent.asRepost(postId: String, postAuthorId: String) = RepostData(
        repostId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        sig = this.sig,
        postId = postId,
        postAuthorId = postAuthorId
    )

    private fun List<JsonArray>.findPostId(): String? {
        val postTag = firstOrNull { it.isEventIdTag() }
        return postTag?.getTagValueOrNull()
    }

    private fun JsonArray.isEventIdTag() = getOrNull(0)?.jsonPrimitive?.content == "e"

    private fun JsonArray.isPubKeyTag() = getOrNull(0)?.jsonPrimitive?.content == "p"

    private fun JsonArray.getTagValueOrNull() = getOrNull(1)?.jsonPrimitive?.content

    private fun JsonArray.hasMentionMarker() = contains(JsonPrimitive("mention"))

    private fun JsonArray.hasReplyMarker() = contains(JsonPrimitive("reply"))

    private fun JsonArray.hasRootMarker() = contains(JsonPrimitive("root"))

    private fun JsonArray.hasAnyMarker() = hasRootMarker() || hasReplyMarker() || hasMentionMarker()

    private fun List<JsonArray>.findPostAuthorId(): String? {
        val postAuthorTag = firstOrNull { it.isPubKeyTag() }
        return postAuthorTag?.getTagValueOrNull()
    }

}
