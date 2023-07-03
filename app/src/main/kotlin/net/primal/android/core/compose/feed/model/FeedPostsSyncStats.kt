package net.primal.android.core.compose.feed.model

data class FeedPostsSyncStats(
    val postsCount: Int = 0,
    val postIds: List<String> = emptyList(),
    val avatarUrls: List<String> = emptyList(),
)
