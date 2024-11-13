package net.primal.android.premium.manage.media.api.model

import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

data class MediaUploadsResponse(
    val paging: ContentPrimalPaging?,
    val cdnResources: List<PrimalEvent>,
    val uploadInfo: PrimalEvent?,
)
