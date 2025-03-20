package net.primal.android.nostr.ext

import net.primal.android.events.domain.CdnResource
import net.primal.android.events.domain.CdnResourceVariant
import net.primal.android.nostr.model.primal.content.ContentPrimalEventResources
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.PrimalEvent

private fun List<PrimalEvent>.flatMapNotNullAsContentPrimalEventResources() =
    mapNotNull { it.content.decodeFromJsonStringOrNull<ContentPrimalEventResources>() }

fun List<PrimalEvent>.flatMapNotNullAsCdnResource() =
    flatMapNotNullAsContentPrimalEventResources()
        .flatMap {
            it.resources.map { eventResource ->
                CdnResource(
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
