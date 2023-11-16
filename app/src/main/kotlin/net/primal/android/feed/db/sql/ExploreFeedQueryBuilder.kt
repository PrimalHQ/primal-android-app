package net.primal.android.feed.db.sql

import androidx.sqlite.db.SimpleSQLiteQuery
import net.primal.android.core.ext.isMostZappedFeed
import net.primal.android.core.ext.isPopularFeed
import net.primal.android.core.ext.isTrendingFeed

class ExploreFeedQueryBuilder(
    private val feedDirective: String,
    private val userPubkey: String,
) : FeedQueryBuilder {

    companion object {
        private const val EXPLORE_BASIC_QUERY = """
            SELECT
                PostData.postId,
                PostData.authorId,
                PostData.createdAt,
                PostData.content,
                PostData.raw,
                PostData.authorMetadataId,
                PostData.hashtags,
                NULL AS repostId,
                NULL AS repostAuthorId,
                PostUserStats.liked AS userLiked,
                PostUserStats.replied AS userReplied,
                PostUserStats.reposted AS userReposted,
                PostUserStats.zapped AS userZapped,
                NULL AS feedCreatedAt,
                CASE WHEN MutedUserData.userId IS NOT NULL THEN 1 ELSE 0 END AS isMuted
            FROM PostData
            INNER JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = PostData.postId
            INNER JOIN PostStats ON PostData.postId = PostStats.postId
            LEFT JOIN PostUserStats ON PostUserStats.postId = PostData.postId AND PostUserStats.userId = ?
            LEFT JOIN MutedUserData ON MutedUserData.userId = PostData.authorId
            WHERE FeedPostDataCrossRef.feedDirective = ? AND isMuted = 0
        """
    }

    private val orderByClause = when {
        feedDirective.isPopularFeed() -> "ORDER BY PostStats.score"
        feedDirective.isTrendingFeed() -> "ORDER BY PostStats.score24h"
        feedDirective.isMostZappedFeed() -> "ORDER BY PostStats.satsZapped"
        else -> throw UnsupportedOperationException("Unsupported explore feed directive.")
    }

    override fun feedQuery(): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$EXPLORE_BASIC_QUERY $orderByClause DESC",
            bindArgs = arrayOf(userPubkey, feedDirective),
        )
    }

    override fun newestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$EXPLORE_BASIC_QUERY $orderByClause DESC LIMIT ?",
            bindArgs = arrayOf(userPubkey, feedDirective, limit),
        )
    }

    override fun oldestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$EXPLORE_BASIC_QUERY $orderByClause ASC LIMIT ?",
            bindArgs = arrayOf(userPubkey, feedDirective, limit),
        )
    }
}
