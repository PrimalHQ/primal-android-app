package net.primal.android.feed.ui.model

import java.time.Instant

data class FeedPostUi(
    val postId: String,
    val repostId: String?,
    val repostAuthorDisplayName: String? = null,
    val authorDisplayName: String,
    val authorInternetIdentifier: String? = null,
    val authorAvatarUrl: String? = null,
    val content: String,
    val urls: List<String>,
    val timestamp: Instant,
    val stats: FeedPostStatsUi,
)

data class FeedPostStatsUi(
    val repliesCount: Int = 0,
    val userReplied: Boolean = false,
    val zapsCount: Int = 0,
    val userZapped: Boolean = false,
    val likesCount: Int = 0,
    val userLiked: Boolean = false,
    val repostsCount: Int = 0,
    val userReposted: Boolean = false,
)
