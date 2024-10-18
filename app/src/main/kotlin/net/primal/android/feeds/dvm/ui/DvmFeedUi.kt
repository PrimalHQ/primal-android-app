package net.primal.android.feeds.dvm.ui

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.feeds.domain.DvmFeed

data class DvmFeedUi(
    val data: DvmFeed,
    val userLiked: Boolean? = false,
    val userZapped: Boolean? = false,
    val totalLikes: Long? = null,
    val totalSatsZapped: Long? = null,
    val actionUserAvatars: List<CdnImage> = emptyList(),
)