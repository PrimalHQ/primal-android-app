package net.primal.android.core.compose.feed.model

import net.primal.android.attachments.domain.CdnImage

data class FeedPostsSyncStats(
    val postsCount: Int = 0,
    val postIds: List<String> = emptyList(),
    val avatarCdnImages: List<CdnImage> = emptyList(),
)
