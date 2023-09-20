package net.primal.android.core.compose.feed.model

data class FeedPostStatsUi(
    val repliesCount: Long = 0,
    val userReplied: Boolean = false,
    val zapsCount: Long = 0,
    val satsZapped: Long = 0,
    val userZapped: Boolean = false,
    val likesCount: Long = 0,
    val userLiked: Boolean = false,
    val repostsCount: Long = 0,
    val userReposted: Boolean = false,
)
