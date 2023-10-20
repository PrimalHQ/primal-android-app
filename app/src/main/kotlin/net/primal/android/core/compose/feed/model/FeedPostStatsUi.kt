package net.primal.android.core.compose.feed.model

import net.primal.android.feed.db.FeedPostUserStats
import net.primal.android.feed.db.PostStats
import net.primal.android.profile.db.PostUserStats

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
) {
    companion object {
        fun from(postStats: PostStats?, userStats: FeedPostUserStats?) =
            FeedPostStatsUi(
                repliesCount = postStats?.replies ?: 0,
                userReplied = userStats?.userReplied ?: false,
                zapsCount = postStats?.zaps ?: 0,
                satsZapped = postStats?.satsZapped ?: 0,
                userZapped = userStats?.userZapped ?: false,
                likesCount = postStats?.likes ?: 0,
                userLiked = userStats?.userLiked ?: false,
                repostsCount = postStats?.reposts ?: 0,
                userReposted = userStats?.userReposted ?: false,
            )

        fun from(postStats: PostStats?, userStats: PostUserStats?) =
            FeedPostStatsUi(
                repliesCount = postStats?.replies ?: 0,
                userReplied = userStats?.replied ?: false,
                zapsCount = postStats?.zaps ?: 0,
                satsZapped = postStats?.satsZapped ?: 0,
                userZapped = userStats?.zapped ?: false,
                likesCount = postStats?.likes ?: 0,
                userLiked = userStats?.liked ?: false,
                repostsCount = postStats?.reposts ?: 0,
                userReposted = userStats?.reposted ?: false,
            )
    }
}
