package net.primal.android.feed.db.sql

import androidx.sqlite.db.SimpleSQLiteQuery
import net.primal.android.core.ext.isExploreMostZapped4hFeed
import net.primal.android.core.ext.isExploreMostZappedFeed
import net.primal.android.core.ext.isExplorePopularFeed
import net.primal.android.core.ext.isExploreTrendingFeed

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
                EventUserStats.liked AS userLiked,
                EventUserStats.replied AS userReplied,
                EventUserStats.reposted AS userReposted,
                EventUserStats.zapped AS userZapped,
                NULL AS feedCreatedAt,
                CASE WHEN MutedUserData.userId IS NOT NULL THEN 1 ELSE 0 END AS isMuted,
                PostData.replyToPostId,
                PostData.replyToAuthorId
            FROM PostData
            INNER JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = PostData.postId
            INNER JOIN EventStats ON PostData.postId = EventStats.eventId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = PostData.postId AND EventUserStats.userId = ?
            LEFT JOIN MutedUserData ON MutedUserData.userId = PostData.authorId
            WHERE FeedPostDataCrossRef.feedDirective = ? AND isMuted = 0
        """
    }

    private val orderByClause = when {
        feedDirective.isExplorePopularFeed() -> "ORDER BY EventStats.score"
        feedDirective.isExploreTrendingFeed() -> "ORDER BY EventStats.score24h"
        feedDirective.isExploreMostZapped4hFeed() -> "ORDER BY EventStats.satsZapped"
        feedDirective.isExploreMostZappedFeed() -> "ORDER BY EventStats.satsZapped"
        else -> "ORDER BY PostData.createdAt"
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
