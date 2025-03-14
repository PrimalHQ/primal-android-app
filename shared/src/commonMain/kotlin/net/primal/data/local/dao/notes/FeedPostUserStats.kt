package net.primal.data.local.dao.notes

data class FeedPostUserStats(
    val userLiked: Boolean = false,
    val userReplied: Boolean = false,
    val userReposted: Boolean = false,
    val userZapped: Boolean = false,
)
