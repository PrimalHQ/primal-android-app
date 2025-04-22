package net.primal.android.feeds.dvm.ui

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.feeds.DvmFeed
import net.primal.domain.links.CdnImage

data class DvmFeedUi(
    val data: DvmFeed,
    val userLiked: Boolean? = false,
    val userZapped: Boolean? = false,
    val totalLikes: Long? = null,
    val totalSatsZapped: Long? = null,
    val actionUserAvatars: List<CdnImage> = emptyList(),
    val actionUserLegendaryCustomizations: List<LegendaryCustomization?> = emptyList(),
)
