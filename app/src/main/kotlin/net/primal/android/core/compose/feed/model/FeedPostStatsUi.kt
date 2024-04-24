package net.primal.android.core.compose.feed.model

import net.primal.android.feed.db.FeedPostUserStats
import net.primal.android.note.db.NoteStats
import net.primal.android.note.db.NoteUserStats

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
        fun from(noteStats: NoteStats?, userStats: FeedPostUserStats?) =
            FeedPostStatsUi(
                repliesCount = noteStats?.replies ?: 0,
                userReplied = userStats?.userReplied ?: false,
                zapsCount = noteStats?.zaps ?: 0,
                satsZapped = noteStats?.satsZapped ?: 0,
                userZapped = userStats?.userZapped ?: false,
                likesCount = noteStats?.likes ?: 0,
                userLiked = userStats?.userLiked ?: false,
                repostsCount = noteStats?.reposts ?: 0,
                userReposted = userStats?.userReposted ?: false,
            )

        fun from(noteStats: NoteStats?, userStats: NoteUserStats?) =
            FeedPostStatsUi(
                repliesCount = noteStats?.replies ?: 0,
                userReplied = userStats?.replied ?: false,
                zapsCount = noteStats?.zaps ?: 0,
                satsZapped = noteStats?.satsZapped ?: 0,
                userZapped = userStats?.zapped ?: false,
                likesCount = noteStats?.likes ?: 0,
                userLiked = userStats?.liked ?: false,
                repostsCount = noteStats?.reposts ?: 0,
                userReposted = userStats?.reposted ?: false,
            )
    }
}
