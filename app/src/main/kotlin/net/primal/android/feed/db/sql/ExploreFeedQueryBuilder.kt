package net.primal.android.feed.db.sql

import androidx.sqlite.db.SimpleSQLiteQuery
import net.primal.android.feed.feed.isMostZappedFeed
import net.primal.android.feed.feed.isPopularFeed
import net.primal.android.feed.feed.isTrendingFeed

class ExploreFeedQueryBuilder(
    private val feedDirective: String,
) : FeedQueryBuilder {

    companion object {
        private const val EXPLORE_BASIC_QUERY = """
            SELECT FeedPostData.* FROM FeedPostData
            INNER JOIN FeedPostDataCrossRef ON FeedPostData.postId = FeedPostDataCrossRef.postId
            INNER JOIN PostStats ON FeedPostData.postId = PostStats.postId
            WHERE FeedPostDataCrossRef.feedDirective = ?
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
            bindArgs = arrayOf(feedDirective),
        )
    }

    override fun newestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$EXPLORE_BASIC_QUERY $orderByClause DESC LIMIT ?",
            bindArgs = arrayOf(feedDirective, limit),
        )
    }

    override fun oldestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$EXPLORE_BASIC_QUERY $orderByClause ASC LIMIT ?",
            bindArgs = arrayOf(feedDirective, limit),
        )
    }

}
