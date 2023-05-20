package net.primal.android.nostr.processor

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.Repost
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.serialization.NostrJson

class RepostEventProcessor(
    private val database: PrimalDatabase,
) : NostrEventProcessor {

    override val kind = NostrEventKind.Reposts

    override fun process(events: List<NostrEvent>) {
        val referencedPosts = events.filter { it.content.isNotEmpty() }
        ShortTextNoteEventProcessor(database = database).process(referencedPosts)

        database.reposts().upsertAll(
            events = events.mapNotNull {
                val (postId, postAuthorId) = it.findPostAndAuthorIds()
                if (postId != null && postAuthorId != null) {
                    it.asRepost(postId, postAuthorId)
                } else null
            }
        )
    }

    private fun NostrEvent.asRepost(postId: String, postAuthorId: String) = Repost(
        repostId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        sig = this.sig,
        postId = postId,
        postAuthorId = postAuthorId
    )

    private fun NostrEvent.findPostAndAuthorIds(): Pair<String?, String?> {
        val contentNostrEvent = if (this.content.isNotEmpty()) {
            NostrJson.decodeFromJsonElement<NostrEvent>(
                NostrJson.parseToJsonElement(this.content)
            )
        } else null

        val postId = contentNostrEvent?.id ?: this.tags.findPostId()
        val postAuthorId = contentNostrEvent?.pubKey ?: this.tags.findPostAuthorId()

        return Pair(postId, postAuthorId)
    }

    private fun List<JsonArray>.findPostId(): String? {
        val postTag = firstOrNull { it.isEventIdTag() && it.hasMentionMarker() }
        return postTag?.getTagValueOrNull()
    }

    private fun JsonArray.isEventIdTag() = getOrNull(0)?.jsonPrimitive?.content == "e"

    private fun JsonArray.isPubKeyTag() = getOrNull(0)?.jsonPrimitive?.content == "p"

    private fun JsonArray.getTagValueOrNull() = getOrNull(1)?.jsonPrimitive?.content

    private fun JsonArray.hasMentionMarker() = contains(JsonPrimitive("mention"))

    private fun List<JsonArray>.findPostAuthorId(): String? {
        val postAuthorTag = firstOrNull { it.isPubKeyTag() }
        return postAuthorTag?.getTagValueOrNull()
    }

}
