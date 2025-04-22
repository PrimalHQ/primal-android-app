package net.primal.android.nostr.ext

import net.primal.android.nostr.model.primal.content.ContentPrimalEventResources
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.common.PrimalEvent
import net.primal.domain.links.CdnResource
import net.primal.domain.links.CdnResourceVariant

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
