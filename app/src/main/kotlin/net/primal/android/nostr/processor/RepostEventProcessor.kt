package net.primal.android.nostr.processor

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.Repost
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.serialization.NostrJson
import timber.log.Timber

class RepostEventProcessor(
    private val database: PrimalDatabase,
) : NostrEventProcessor {

    override val kind = NostrEventKind.Reposts

    override fun process(events: List<NostrEvent>) {
        val referencedPosts = events.filter { it.content.isNotEmpty() }
        ShortTextNoteEventProcessor(database = database).process(referencedPosts)

        database.reposts().upsertAll(
            events = referencedPosts.mapNotNull {
                it.asRepost()
            }
        )

        events.filter { it.content.isEmpty() }.forEach {
            Timber.e("Unable to process following nostr event: $it")
        }
    }

    private fun NostrEvent.asRepost(): Repost? {
        val contentNostrEvent = NostrJson.decodeFromJsonElement<NostrEvent>(
            NostrJson.parseToJsonElement(this.content)
        )

        val postId = contentNostrEvent.id
        val postAuthorId = contentNostrEvent.pubKey

        return Repost(
            eventId = this.id,
            authorId = this.pubKey,
            createdAt = this.createdAt,
            tags = this.tags,
            sig = this.sig,
            postId = postId,
            postAuthorId = postAuthorId
        )
    }

}
