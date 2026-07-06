package net.primal.data.remote.mapper

import net.primal.domain.links.CdnResource

/** Both CDN projections derived from a single decode of the same `PrimalEvent` list. */
data class CdnResourcesAndThumbnails(
    val cdnResources: List<CdnResource>,
    val videoThumbnails: Map<String, String>,
)
