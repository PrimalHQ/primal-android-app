package net.primal.data.remote.mapper

import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.remote.model.ContentPrimalLinkPreviews
import net.primal.domain.common.PrimalEvent
import net.primal.domain.links.EventLinkPreviewData

fun List<PrimalEvent>.flatMapNotNullAsLinkPreviewResource(): List<EventLinkPreviewData> =
    mapNotNull { it.content.decodeFromJsonStringOrNull<ContentPrimalLinkPreviews>() }
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
