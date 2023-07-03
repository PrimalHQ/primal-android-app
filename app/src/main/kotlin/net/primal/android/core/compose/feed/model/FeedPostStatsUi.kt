package net.primal.android.core.compose.feed.model

data class FeedPostStatsUi(
    val repliesCount: Int = 0,
    val userReplied: Boolean = false,
    val zapsCount: Int = 0,
    val satsZapped: Int = 0,
    val userZapped: Boolean = false,
    val likesCount: Int = 0,
    val userLiked: Boolean = false,
    val repostsCount: Int = 0,
    val userReposted: Boolean = false,
)
