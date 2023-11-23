package net.primal.android.nostr.ext

import net.primal.android.attachments.domain.CdnResource
import net.primal.android.attachments.domain.CdnResourceVariant
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventResources

private fun List<PrimalEvent>.flatMapNotNullAsContentPrimalEventResources() =
    mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventResources>(it.content) }

fun List<PrimalEvent>.flatMapNotNullAsCdnResource() =
    flatMapNotNullAsContentPrimalEventResources()
        .flatMap {
            val eventId = it.eventId
            it.resources.map { eventResource ->
                CdnResource(
                    eventId = eventId,
                    contentType = eventResource.mimeType,
                    url = eventResource.url,
                    variants = eventResource.variants.map { variant ->
                        CdnResourceVariant(
                            width = variant.width,
                            height = variant.height,
                            mediaUrl = variant.mediaUrl,
                        )
                    },
                )
            }
        }

fun List<PrimalEvent>.flatMapNotNullAsVideoThumbnailsMap(): Map<String, String> {
    val thumbnailsMap = hashMapOf<String, String>()
    flatMapNotNullAsContentPrimalEventResources()
        .map { it.videoThumbnails }
        .fold(thumbnailsMap) { finalMap, currentMap ->
            finalMap.apply { putAll(currentMap) }
        }
    return thumbnailsMap
}
