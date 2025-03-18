package net.primal.data.remote.mapper

import net.primal.core.utils.decodeFromStringOrNull
import net.primal.data.remote.model.ContentPrimalEventResources
import net.primal.data.serialization.NostrJson
import net.primal.domain.CdnResource
import net.primal.domain.CdnResourceVariant
import net.primal.domain.PrimalEvent

private fun List<PrimalEvent>.flatMapNotNullAsContentPrimalEventResources() =
    mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventResources>(it.content) }

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
