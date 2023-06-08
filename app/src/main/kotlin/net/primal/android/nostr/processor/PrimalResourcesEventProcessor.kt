package net.primal.android.nostr.processor

import kotlinx.serialization.decodeFromString
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.PostResource
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventResources
import net.primal.android.nostr.model.primal.content.EventResource
import net.primal.android.serialization.NostrJson

class PrimalResourcesEventProcessor(
    private val database: PrimalDatabase
) : PrimalEventProcessor {

    override suspend fun process(events: List<PrimalEvent>) {
        database.resources().upsert(
            data = events
                .map { NostrJson.decodeFromString<ContentPrimalEventResources>(it.content) }
                .flatMap {
                    val eventId = it.eventId
                    it.resources.map { eventResource ->
                        eventResource.asPostResourcePO(postId = eventId)
                    }
                }
        )
    }

    private fun EventResource.asPostResourcePO(postId: String) = PostResource(
        postId = postId,
        contentType = this.mimeType,
        url = this.url,
        variants = this.variants,
    )
}
