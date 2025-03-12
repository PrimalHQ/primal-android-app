package net.primal.repository.processors.mappers

import net.primal.domain.EventLinkPreviewData
import net.primal.networking.model.primal.PrimalEvent
import net.primal.networking.model.primal.content.ContentPrimalLinkPreviews
import net.primal.serialization.json.NostrJson
import net.primal.serialization.json.decodeFromStringOrNull

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
