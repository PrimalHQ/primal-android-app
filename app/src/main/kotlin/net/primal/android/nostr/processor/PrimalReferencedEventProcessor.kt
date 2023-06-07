package net.primal.android.nostr.processor

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.asPost
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.serialization.NostrJson

class PrimalReferencedEventProcessor(
    private val database: PrimalDatabase
) : PrimalEventProcessor {

    override fun process(events: List<PrimalEvent>) {
        database.posts().upsertAll(
            data = events
                .mapNotNull { it.takeContentOrNull() }
                .map { it.asPost() }
        )
    }

    private fun PrimalEvent.takeContentOrNull(): NostrEvent? {
        return try {
            NostrJson.decodeFromJsonElement<NostrEvent>(
                NostrJson.parseToJsonElement(this.content)
            )
        } catch (error: IllegalArgumentException) {
            null
        }
    }

}