package net.primal.android.feed.db.sql

import androidx.sqlite.db.SimpleSQLiteQuery

class LatestFeedQueryBuilder(
    private val feedDirective: String,
) : FeedQueryBuilder {

    companion object {
        private const val LATEST_BASIC_QUERY = """
            SELECT FeedPostData.* FROM FeedPostData
            INNER JOIN FeedPostDataCrossRef ON FeedPostData.postId = FeedPostDataCrossRef.postId
            WHERE FeedPostDataCrossRef.feedDirective = ?
        """
    }

    override fun feedQuery(): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt DESC",
            bindArgs = arrayOf(feedDirective),
        )
    }

    override fun newestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt DESC LIMIT ?",
            bindArgs = arrayOf(feedDirective, limit),
        )
    }

    override fun oldestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt ASC LIMIT ?",
            bindArgs = arrayOf(feedDirective, limit),
        )
    }
}
