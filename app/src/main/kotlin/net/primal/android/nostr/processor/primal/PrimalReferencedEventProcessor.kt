package net.primal.android.nostr.processor.primal

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.asPost
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.NostrPrimalEvent
import net.primal.android.serialization.NostrJson

class PrimalReferencedEventProcessor(
    private val database: PrimalDatabase
) : NostrPrimalEventProcessor {

    override fun process(events: List<NostrPrimalEvent>) {
        database.posts().upsertAll(
            data = events
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

}