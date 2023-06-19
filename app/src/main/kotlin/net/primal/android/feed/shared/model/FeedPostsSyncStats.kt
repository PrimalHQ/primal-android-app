package net.primal.android.feed.shared.model

data class FeedPostsSyncStats(
    val postsCount: Int = 0,
    val postIds: List<String> = emptyList(),
    val avatarUrls: List<String> = emptyList(),
)
