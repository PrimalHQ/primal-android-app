package net.primal.android.nostr.processor

import androidx.room.withTransaction
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.flatMapAsPostResources
import net.primal.android.nostr.ext.mapNotNullAsPost
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.serialization.NostrJson

class PrimalReferencedEventProcessor(
    private val database: PrimalDatabase
) : PrimalEventProcessor {

    override suspend fun process(events: List<PrimalEvent>) {
        val posts = events
            .mapNotNull { it.takeContentOrNull() }
            .mapNotNullAsPost()

        database.withTransaction {
            database.posts().upsertAll(data = posts)
            database.resources().insertOrIgnore(data = posts.flatMapAsPostResources())
        }
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