package net.primal.android.nostr.ext

import net.primal.android.nostr.model.primal.content.ContentPrimalLinkPreviews
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.EventLinkPreviewData
import net.primal.domain.PrimalEvent

fun List<PrimalEvent>.flatMapNotNullAsLinkPreviewResource() =
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
