package net.primal.android.nostr.ext

import net.primal.android.feed.db.MediaResource
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventResources
import net.primal.android.nostr.model.primal.content.EventResource
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull

fun EventResource.asMediaResourcePO(eventId: String) = MediaResource(
    eventId = eventId,
    contentType = this.mimeType,
    url = this.url,
    variants = this.variants,
)

fun List<PrimalEvent>.flatMapNotNullAsMediaResourcePO() =
    this.mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventResources>(it.content) }
        .flatMap {
            val eventId = it.eventId
            it.resources.map { eventResource ->
                eventResource.asMediaResourcePO(eventId = eventId)
            }
        }
