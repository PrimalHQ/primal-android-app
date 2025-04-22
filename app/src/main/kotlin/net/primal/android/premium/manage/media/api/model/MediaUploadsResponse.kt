package net.primal.android.premium.manage.media.api.model

import net.primal.domain.common.ContentPrimalPaging
import net.primal.domain.common.PrimalEvent

data class MediaUploadsResponse(
    val paging: ContentPrimalPaging?,
    val cdnResources: List<PrimalEvent>,
    val uploadInfo: PrimalEvent?,
)
