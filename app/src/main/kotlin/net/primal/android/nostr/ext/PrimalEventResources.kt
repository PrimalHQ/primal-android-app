package net.primal.android.nostr.ext

import net.primal.android.feed.db.MediaResource
import net.primal.android.nostr.model.primal.content.EventResource

fun EventResource.asMediaResourcePO(eventId: String) = MediaResource(
    eventId = eventId,
    contentType = this.mimeType,
    url = this.url,
    variants = this.variants,
)
