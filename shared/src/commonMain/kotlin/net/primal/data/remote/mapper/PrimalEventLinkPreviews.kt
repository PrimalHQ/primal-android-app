package net.primal.data.remote.mapper

import net.primal.core.utils.decodeFromStringOrNull
import net.primal.data.remote.model.ContentPrimalLinkPreviews
import net.primal.data.serialization.NostrJson
import net.primal.domain.EventLinkPreviewData
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
