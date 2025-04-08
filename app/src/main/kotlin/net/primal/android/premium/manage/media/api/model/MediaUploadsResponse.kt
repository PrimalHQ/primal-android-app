package net.primal.android.premium.manage.media.api.model

import net.primal.domain.ContentPrimalPaging
import net.primal.domain.PrimalEvent

data class MediaUploadsResponse(
    val paging: ContentPrimalPaging?,
    val cdnResources: List<PrimalEvent>,
    val uploadInfo: PrimalEvent?,
)
