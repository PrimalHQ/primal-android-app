package net.primal.android.nostr.primal.processor

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.PostData
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.primal.model.NostrPrimalEvent
import net.primal.android.serialization.NostrJson

class PrimalReferencedEventProcessor(
    private val database: PrimalDatabase
) : NostrPrimalEventProcessor {

    override val kind = NostrEventKind.PrimalReferencedEvent

    override fun process(events: List<NostrPrimalEvent>) {
        database.posts().upsertAll(
            events = events
                .mapNotNull { it.takeContentOrNull() }
                .map { it.asPost() }
        )
    }

    private fun NostrPrimalEvent.takeContentOrNull(): NostrEvent? {
        return try {
            NostrJson.decodeFromJsonElement<NostrEvent>(
                NostrJson.parseToJsonElement(this.content)
            )
        } catch (error: IllegalArgumentException) {
            null
        }
    }

    private fun NostrEvent.asPost(): PostData = PostData(
        postId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        content = this.content,
        sig = this.sig,
    )
}