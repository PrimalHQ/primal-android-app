package net.primal.android.nostr.ext

import net.primal.android.attachments.domain.CdnResource
import net.primal.android.attachments.domain.CdnResourceVariant
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventResources
import net.primal.android.serialization.json.NostrJson
import net.primal.android.serialization.json.decodeFromStringOrNull

fun List<PrimalEvent>.flatMapNotNullAsCdnResource() =
    mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventResources>(it.content) }
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
