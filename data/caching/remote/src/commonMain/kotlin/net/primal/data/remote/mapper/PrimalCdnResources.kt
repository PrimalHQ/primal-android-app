package net.primal.data.remote.mapper

import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.remote.model.ContentPrimalEventResources
import net.primal.domain.common.PrimalEvent
import net.primal.domain.links.CdnResource
import net.primal.domain.links.CdnResourceVariant

private fun List<PrimalEvent>.flatMapNotNullAsContentPrimalEventResources(): List<ContentPrimalEventResources> =
    mapNotNull { it.content.decodeFromJsonStringOrNull<ContentPrimalEventResources>() }

private fun ContentPrimalEventResources.toCdnResources(): List<CdnResource> =
    resources.map { eventResource ->
        CdnResource(
            contentType = eventResource.mimeType,
            url = eventResource.url,
            variants = eventResource.variants.map { variant ->
                CdnResourceVariant(
                    width = variant.width,
                    height = variant.height,
                    mediaUrl = variant.mediaUrl,
                    durationInSeconds = variant.duration?.toDouble()?.takeIf { it > 0.0 },
                )
            },
        )
    }

private fun List<ContentPrimalEventResources>.foldVideoThumbnails(): Map<String, String> {
    val thumbnailsMap = hashMapOf<String, String>()
    forEach { thumbnailsMap.putAll(it.videoThumbnails) }
    return thumbnailsMap
}

fun List<PrimalEvent>.flatMapNotNullAsCdnResource(): List<CdnResource> =
    flatMapNotNullAsContentPrimalEventResources().flatMap { it.toCdnResources() }

fun List<PrimalEvent>.flatMapNotNullAsVideoThumbnailsMap(): Map<String, String> =
    flatMapNotNullAsContentPrimalEventResources().foldVideoThumbnails()

/**
 * Decodes the CDN resource events ONCE and projects both the [CdnResource] list and the video-thumbnails map from
 * that single pass. Equivalent to calling [flatMapNotNullAsCdnResource] and [flatMapNotNullAsVideoThumbnailsMap]
 * separately (same content, same order), but without decoding the events twice.
 */
fun List<PrimalEvent>.flatMapNotNullAsCdnResourcesAndThumbnails(): CdnResourcesAndThumbnails {
    val contentResources = flatMapNotNullAsContentPrimalEventResources()
    return CdnResourcesAndThumbnails(
        cdnResources = contentResources.flatMap { it.toCdnResources() },
        videoThumbnails = contentResources.foldVideoThumbnails(),
    )
}
