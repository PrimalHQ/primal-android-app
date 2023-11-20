package net.primal.android.core.compose.feed.model

import net.primal.android.attachments.domain.CdnResourceVariant

data class FeedPostsSyncStats(
    val postsCount: Int = 0,
    val postIds: List<String> = emptyList(),
    val avatarUrls: List<String> = emptyList(),
    val avatarVariantsMap: Map<String, List<CdnResourceVariant>> = emptyMap(),
)
