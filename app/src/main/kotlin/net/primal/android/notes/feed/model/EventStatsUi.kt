package net.primal.android.notes.feed.model

import net.primal.android.events.db.EventStats
import net.primal.android.events.db.EventUserStats
import net.primal.android.notes.db.FeedPostUserStats
import net.primal.domain.model.FeedPostStats
import net.primal.domain.model.NostrEventStats
import net.primal.domain.model.NostrEventUserStats

data class EventStatsUi(
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
        fun from(eventStats: EventStats?, feedPostUserStats: FeedPostUserStats?) =
            EventStatsUi(
                repliesCount = eventStats?.replies ?: 0,
                userReplied = feedPostUserStats?.userReplied ?: false,
                zapsCount = eventStats?.zaps ?: 0,
                satsZapped = eventStats?.satsZapped ?: 0,
                userZapped = feedPostUserStats?.userZapped ?: false,
                likesCount = eventStats?.likes ?: 0,
                userLiked = feedPostUserStats?.userLiked ?: false,
                repostsCount = eventStats?.reposts ?: 0,
                userReposted = feedPostUserStats?.userReposted ?: false,
            )

        fun from(eventStats: EventStats?, userStats: EventUserStats?) =
            EventStatsUi(
                repliesCount = eventStats?.replies ?: 0,
                userReplied = userStats?.replied ?: false,
                zapsCount = eventStats?.zaps ?: 0,
                satsZapped = eventStats?.satsZapped ?: 0,
                userZapped = userStats?.zapped ?: false,
                likesCount = eventStats?.likes ?: 0,
                userLiked = userStats?.liked ?: false,
                repostsCount = eventStats?.reposts ?: 0,
                userReposted = userStats?.reposted ?: false,
            )

        fun from(eventStats: NostrEventStats?, userStats: NostrEventUserStats?) =
            EventStatsUi(
                repliesCount = eventStats?.replies ?: 0,
                userReplied = userStats?.replied ?: false,
                zapsCount = eventStats?.zaps ?: 0,
                satsZapped = eventStats?.satsZapped ?: 0,
                userZapped = userStats?.zapped ?: false,
                likesCount = eventStats?.likes ?: 0,
                userLiked = userStats?.liked ?: false,
                repostsCount = eventStats?.reposts ?: 0,
                userReposted = userStats?.reposted ?: false,
            )

        fun from(stats: FeedPostStats?) =
            EventStatsUi(
                repliesCount = stats?.repliesCount ?: 0,
                userReplied = stats?.userReplied == true,
                zapsCount = stats?.zapsCount ?: 0,
                satsZapped = stats?.satsZapped ?: 0,
                userZapped = stats?.userZapped == true,
                likesCount = stats?.likesCount ?: 0,
                userLiked = stats?.userLiked == true,
                repostsCount = stats?.repostsCount ?: 0,
                userReposted = stats?.userReposted == true,
            )
    }
}
