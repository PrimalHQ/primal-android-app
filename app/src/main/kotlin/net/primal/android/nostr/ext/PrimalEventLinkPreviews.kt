package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.events.domain.EventLinkPreviewData
import net.primal.android.nostr.model.primal.content.ContentPrimalLinkPreviews
import net.primal.domain.PrimalEvent

fun List<PrimalEvent>.flatMapNotNullAsLinkPreviewResource() =
    mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalLinkPreviews>(it.content) }
        .flatMap {
            it.resources.map { eventResource ->
                EventLinkPreviewData(
                    url = eventResource.url,
                    mimeType = eventResource.mimeType,
                    title = eventResource.title,
                    description = eventResource.description,
                    thumbnailUrl = eventResource.thumbnailUrl,
                    authorAvatarUrl = eventResource.iconUrl,
                )
            }
        }
